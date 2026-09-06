package com.iboot.pf.module.mes.controller.admin.prodorder;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderActionReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderCloseReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderCloseRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderKitCheckReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderKitCheckRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderReleaseReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitCheckService;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import com.iboot.pf.module.mes.service.prodorder.MesBackflushAvailabilityChecker;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderActionBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderCloseResultBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
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

@Tag(name = "管理后台 - MES-生产工单")
@RestController
@RequestMapping("/mes/prod-order")
@Validated
public class ProdOrderController {

    /** codegen Service，负责建改删与列表、详情、导出、导入。 */
    @Resource
    private ProdOrderService prodOrderService;
    /** 领域 Service，承担全部状态推进：工单状态只能由这里改。 */
    @Resource
    private MesProdOrderDomainService mesProdOrderDomainService;
    /** 齐套检查动作。 */
    @Resource
    private MesKitCheckService mesKitCheckService;
    /** 关闭前的倒冲料可用量前置校验，只为把物料编码与缺口量写进错误消息。 */
    @Resource
    private MesBackflushAvailabilityChecker backflushAvailabilityChecker;

    @PostMapping("/create")
    @Operation(summary = "创建生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:create')")
    public CommonResult<Long> createProdOrder(@Valid @RequestBody ProdOrderSaveReqVO createReqVO) {
        return success(prodOrderService.createProdOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:update')")
    public CommonResult<Boolean> updateProdOrder(@Valid @RequestBody ProdOrderSaveReqVO updateReqVO) {
        prodOrderService.updateProdOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrder(@RequestParam("id") Long id) {
        prodOrderService.deleteProdOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除生产工单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrderList(@RequestParam("ids") List<Long> ids) {
        prodOrderService.deleteProdOrderListByIds(ids);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/release")
    @Operation(summary = "下达生产工单，锁 BOM 与工艺版本并展开作业工单与工序任务")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:release')")
    public CommonResult<Boolean> releaseProdOrder(@Valid @RequestBody ProdOrderReleaseReqVO reqVO) {
        MesReleaseOrderBO bo = new MesReleaseOrderBO();
        bo.setProdOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        mesProdOrderDomainService.releaseProdOrder(bo);
        return success(true);
    }

    @PostMapping("/hold")
    @Operation(summary = "挂起生产工单，生产中推进到已挂起")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:hold')")
    public CommonResult<Boolean> holdProdOrder(@Valid @RequestBody ProdOrderActionReqVO reqVO) {
        mesProdOrderDomainService.holdProdOrder(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/resume")
    @Operation(summary = "恢复生产工单，已挂起推进回生产中")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:resume')")
    public CommonResult<Boolean> resumeProdOrder(@Valid @RequestBody ProdOrderActionReqVO reqVO) {
        mesProdOrderDomainService.resumeProdOrder(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭生产工单，释放剩余预留、倒冲件反冲扣料、回写需求状态，并返回多余领料清单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:close')")
    public CommonResult<ProdOrderCloseRespVO> closeProdOrder(@Valid @RequestBody ProdOrderCloseReqVO reqVO) {
        // 关闭会给倒冲件按实际产量反冲扣料，可用量不足时抛错的是 WMS，那条消息里只有物料编号。
        // 先批量查一次可用量把物料编码与缺口量带出来，最终防线仍是过账那一步
        backflushAvailabilityChecker.validateBeforeClose(reqVO.getId(), reqVO.getWhId());
        MesCloseOrderBO bo = new MesCloseOrderBO();
        bo.setProdOrderId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        bo.setWhId(reqVO.getWhId());
        bo.setLocId(reqVO.getLocId());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return success(toCloseRespVO(mesProdOrderDomainService.closeProdOrder(bo)));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消生产工单，级联取消作业工单与工序任务")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:cancel')")
    public CommonResult<Boolean> cancelProdOrder(@Valid @RequestBody ProdOrderActionReqVO reqVO) {
        mesProdOrderDomainService.cancelProdOrder(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/check-kit")
    @Operation(summary = "齐套检查，落齐套快照并返回整体结论与缺料明细；全齐时把工单推到待开工")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:check-kit')")
    public CommonResult<ProdOrderKitCheckRespVO> checkKit(@Valid @RequestBody ProdOrderKitCheckReqVO reqVO) {
        MesKitCheckBO bo = new MesKitCheckBO();
        bo.setCheckScope(reqVO.getCheckScope());
        bo.setProdOrderId(reqVO.getId());
        bo.setWorkOrderId(reqVO.getWorkOrderId());
        bo.setOpTaskId(reqVO.getOpTaskId());
        bo.setWhId(reqVO.getWhId());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return success(toKitCheckRespVO(mesKitCheckService.checkKit(bo)));
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<ProdOrderRespVO> getProdOrder(@RequestParam("id") Long id) {
        return success(prodOrderService.getProdOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产工单分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<PageResult<ProdOrderRespVO>> getProdOrderPage(@Valid ProdOrderPageReqVO pageReqVO) {
        return success(prodOrderService.getProdOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdOrderExcel(@Valid ProdOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdOrderRespVO> list = prodOrderService.getProdOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-生产工单.xls", "数据", ProdOrderRespVO.class, list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得生产工单导入模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产工单导入模板.xls", "数据",
                ProdOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入生产工单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public CommonResult<ProdOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdOrderImportExcelVO> list = ExcelUtils.read(file, ProdOrderImportExcelVO.class);
        return success(prodOrderService.importProdOrderList(list));
    }

    // ==================== 内部转换 ====================

    /** 挂起、恢复、取消共用一个 BO：三个动作的入参完全相同，目标状态由动作名与状态机决定。 */
    private MesOrderActionBO toActionBO(ProdOrderActionReqVO reqVO) {
        MesOrderActionBO bo = new MesOrderActionBO();
        bo.setProdOrderId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return bo;
    }

    /**
     * 多余领料清单原样转出。
     *
     * <p>嵌套列表显式逐元素转换，不依赖 hutool 对集合元素类型的推导 —— 靠推导会把领域侧的 BO 直接塞进
     * VO 的列表里，序列化看着正常，取用时才炸。
     */
    private ProdOrderCloseRespVO toCloseRespVO(MesOrderCloseResultBO result) {
        ProdOrderCloseRespVO respVO = new ProdOrderCloseRespVO();
        respVO.setUnreturnedLines(BeanUtils.toBean(result.getUnreturnedLines(),
                ProdOrderCloseRespVO.UnreturnedLine.class));
        return respVO;
    }

    /** 齐套结论与明细行一起转出：缺料时车间要的是"缺什么缺多少"，只给整体结论等于没做这次检查。 */
    private ProdOrderKitCheckRespVO toKitCheckRespVO(MesKitCheckResultBO result) {
        ProdOrderKitCheckRespVO respVO = BeanUtils.toBean(result, ProdOrderKitCheckRespVO.class);
        respVO.setLines(BeanUtils.toBean(result.getLines(), ProdOrderKitCheckRespVO.Line.class));
        return respVO;
    }

    /**
     * 补齐命令的来源与幂等键。
     *
     * <p>来源模块固定 {@code UI}：界面点出来的动作就是后台人工，不听前端的。
     *
     * <p>幂等键原样透传，<b>为空是常态</b>：领域 Service 会按 {@code UI:{工单号}:{命令类型}:0} 确定性派生。
     * 同一张单的同一个动作恒为同一个键，界面连点两次、浏览器重发都会被 {@code mes_command_record}
     * 的唯一索引判重；前端每次点击生成 uuid 或时间戳，两次点击就是两个键，幂等等于没有。
     * 下达与关闭这两个动作对键的依赖最强：下达会展开一整批工序任务，关闭会给 WMS 派生释放预留与反冲的键。
     *
     * <p>来源单号留空：领域侧会兜底成工单号，比数字主键在命令记录里更可读。
     */
    private void fillCommand(MesCommandReqDTO command, String idempotencyKey) {
        command.setSourceModule(MesSourceModuleEnum.UI.name());
        command.setIdempotencyKey(idempotencyKey);
    }

}
