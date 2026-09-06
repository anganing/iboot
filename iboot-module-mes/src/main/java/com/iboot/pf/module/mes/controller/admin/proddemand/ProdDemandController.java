package com.iboot.pf.module.mes.controller.admin.proddemand;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandActionReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandPageReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandToOrderReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.proddemand.MesProdDemandDomainService;
import com.iboot.pf.module.mes.service.proddemand.ProdDemandService;
import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import com.iboot.pf.module.mes.service.proddemand.bo.MesDemandToOrderBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES-生产需求")
@RestController
@RequestMapping("/mes/prod-demand")
@Validated
public class ProdDemandController {

    /** codegen Service，负责建改删与列表、详情、导出、导入。 */
    @Resource
    private ProdDemandService prodDemandService;
    /** 领域 Service，负责需求状态推进与"只有新建态可改删"这条状态规则。 */
    @Resource
    private MesProdDemandDomainService mesProdDemandDomainService;

    @PostMapping("/create")
    @Operation(summary = "创建生产需求")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:create')")
    public CommonResult<Long> createProdDemand(@Valid @RequestBody ProdDemandSaveReqVO createReqVO) {
        return success(prodDemandService.createProdDemand(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产需求，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:update')")
    public CommonResult<Boolean> updateProdDemand(@Valid @RequestBody ProdDemandSaveReqVO updateReqVO) {
        mesProdDemandDomainService.validateDemandDraft(updateReqVO.getId());
        prodDemandService.updateProdDemand(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产需求，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:delete')")
    public CommonResult<Boolean> deleteProdDemand(@RequestParam("id") Long id) {
        mesProdDemandDomainService.validateDemandDraft(id);
        prodDemandService.deleteProdDemand(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除生产需求，仅新建状态可删")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:delete')")
    public CommonResult<Boolean> deleteProdDemandList(@RequestParam("ids") List<Long> ids) {
        mesProdDemandDomainService.validateDemandDraft(ids);
        prodDemandService.deleteProdDemandListByIds(ids);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/confirm")
    @Operation(summary = "确认生产需求，新建推进到已确认")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:confirm')")
    public CommonResult<Boolean> confirmProdDemand(@Valid @RequestBody ProdDemandActionReqVO reqVO) {
        mesProdDemandDomainService.confirmDemand(toConfirmBO(reqVO));
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消生产需求，非终态推进到已取消")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:cancel')")
    public CommonResult<Boolean> cancelProdDemand(@Valid @RequestBody ProdDemandActionReqVO reqVO) {
        mesProdDemandDomainService.cancelDemand(toConfirmBO(reqVO));
        return success(true);
    }

    @PostMapping("/to-prod-order")
    @Operation(summary = "生产需求转生产工单，支持分批转单，转满才置已下达")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:to-prod-order')")
    public CommonResult<Long> toProdOrder(@Valid @RequestBody ProdDemandToOrderReqVO reqVO) {
        MesDemandToOrderBO bo = new MesDemandToOrderBO();
        bo.setDemandId(reqVO.getId());
        bo.setQty(reqVO.getQty());
        bo.setWorkshopId(reqVO.getWorkshopId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return success(mesProdDemandDomainService.demandToProdOrder(bo));
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产需求")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:query')")
    public CommonResult<ProdDemandRespVO> getProdDemand(@RequestParam("id") Long id) {
        return success(prodDemandService.getProdDemand(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产需求分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:query')")
    public CommonResult<PageResult<ProdDemandRespVO>> getProdDemandPage(@Valid ProdDemandPageReqVO pageReqVO) {
        return success(prodDemandService.getProdDemandPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产需求 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdDemandExcel(@Valid ProdDemandPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdDemandRespVO> list = prodDemandService.getProdDemandPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-生产需求.xls", "数据", ProdDemandRespVO.class, list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得生产需求导入模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产需求导入模板.xls", "数据",
                ProdDemandImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入生产需求")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:import')")
    public CommonResult<ProdDemandImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdDemandImportExcelVO> list = ExcelUtils.read(file, ProdDemandImportExcelVO.class);
        return success(prodDemandService.importProdDemandList(list));
    }

    // ==================== 内部转换 ====================

    /** 确认与取消共用一个 BO：两个动作的入参完全相同，动作语义由调用的方法区分。 */
    private MesConfirmDemandBO toConfirmBO(ProdDemandActionReqVO reqVO) {
        MesConfirmDemandBO bo = new MesConfirmDemandBO();
        bo.setDemandId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return bo;
    }

    /**
     * 补齐命令的来源与幂等键。
     *
     * <p>来源模块固定 {@code UI}：界面点出来的动作就是后台人工，不听前端的。
     *
     * <p>幂等键原样透传，<b>为空是常态</b>：领域 Service 会按
     * {@code UI:{需求编号}:{命令类型}:{行号}} 确定性派生。派生而不是让前端造键，是因为同一张单的同一个动作
     * 必须恒为同一个键，界面连点两次才会被 {@code mes_command_record} 的唯一索引判重；
     * 前端每次点击生成 uuid 或时间戳，两次点击就是两个键，幂等等于没有。
     *
     * <p>来源单号也留空：领域侧会兜底成单据编号，比数字主键在命令记录里更可读。
     */
    private void fillCommand(MesCommandReqDTO command, String idempotencyKey) {
        command.setSourceModule(MesSourceModuleEnum.UI.name());
        command.setIdempotencyKey(idempotencyKey);
    }

}
