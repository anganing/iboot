package com.iboot.pf.module.mes.controller.admin.materialreq;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqActionReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqConfirmReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqCreateReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqSaveReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.materialreq.MaterialReqService;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
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

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 领退补料申请管理端接口。
 *
 * <p>创建走领域 Service：明细留空时按用料关系自动展开（工序级取路线用料、整单取 BOM），
 * 与齐套用同一套需求量口径。三个动作端点（提交、确认、取消）带库存后果，全部转调领域 Service。
 *
 * <p>修改、删除与明细行的增删改都只在<b>新建状态</b>下允许，校验由领域 Service 的
 * {@code validateReqDraft} / {@code validateLineDraft} 给出，本类只转调：单据一提交就已经占了 WMS 的预留，
 * 那之后改明细会让预留与明细脱节。
 *
 * <p>明细子表挂在父资源路径下，权限沿用父资源的 query / create / update / delete。
 */
@Tag(name = "管理后台 - MES-领退补料申请")
@RestController
@RequestMapping("/mes/material-req")
@Validated
public class MaterialReqController {

    /** codegen Service，负责手工改单、明细维护与列表、详情、导出。 */
    @Resource
    private MaterialReqService materialReqService;
    /** 领域 Service，负责按用料关系展开明细，以及一切带库存后果的状态推进。 */
    @Resource
    private MesMaterialReqDomainService mesMaterialReqDomainService;

    @PostMapping("/create")
    @Operation(summary = "创建领退补料申请，明细留空时按工序用料或整单 BOM 自动展开")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReq(@Valid @RequestBody MaterialReqCreateReqVO createReqVO) {
        MesCreateMaterialReqBO bo = BeanUtils.toBean(createReqVO, MesCreateMaterialReqBO.class);
        // 嵌套列表显式逐元素转换，不依赖 hutool 对集合元素类型的推导
        bo.setLines(BeanUtils.toBean(createReqVO.getLines(), MesCreateMaterialReqBO.Line.class));
        fillCommand(bo, createReqVO.getIdempotencyKey());
        return success(mesMaterialReqDomainService.createMaterialReq(bo));
    }

    @PutMapping("/update")
    @Operation(summary = "更新领退补料申请，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReq(@Valid @RequestBody MaterialReqSaveReqVO updateReqVO) {
        mesMaterialReqDomainService.validateReqDraft(updateReqVO.getId());
        materialReqService.updateMaterialReq(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除领退补料申请，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReq(@RequestParam("id") Long id) {
        mesMaterialReqDomainService.validateReqDraft(id);
        materialReqService.deleteMaterialReq(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除领退补料申请，仅新建状态可删")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqList(@RequestParam("ids") List<Long> ids) {
        mesMaterialReqDomainService.validateReqDraft(ids);
        materialReqService.deleteMaterialReqListByIds(ids);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/submit")
    @Operation(summary = "提交领退补料申请，领料与补料逐行占库存预留")
    @PreAuthorize("@ss.hasPermission('mes:material-req:submit')")
    public CommonResult<Boolean> submitMaterialReq(@Valid @RequestBody MaterialReqActionReqVO reqVO) {
        mesMaterialReqDomainService.submitMaterialReq(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认领退补料申请，建 WMS 出入库单并过账，可逐行指定确认数量与库位")
    @PreAuthorize("@ss.hasPermission('mes:material-req:confirm')")
    public CommonResult<Boolean> confirmMaterialReq(@Valid @RequestBody MaterialReqConfirmReqVO reqVO) {
        MesConfirmMaterialReqBO bo = new MesConfirmMaterialReqBO();
        bo.setMaterialReqId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        // 逐行的确认数量与库位必须传下去：预留行的库位在生产环境可能为空，确认时回落取不到，
        // 只能由现场逐行指定；漏传这一层，发料会因"未指定库位"直接失败
        bo.setLines(BeanUtils.toBean(reqVO.getLines(), MesConfirmMaterialReqBO.Line.class));
        fillCommand(bo, reqVO.getIdempotencyKey());
        mesMaterialReqDomainService.confirmMaterialReq(bo);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消领退补料申请，已提交的先释放预留再取消")
    @PreAuthorize("@ss.hasPermission('mes:material-req:cancel')")
    public CommonResult<Boolean> cancelMaterialReq(@Valid @RequestBody MaterialReqActionReqVO reqVO) {
        mesMaterialReqDomainService.cancelMaterialReq(toActionBO(reqVO));
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得领退补料申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<MaterialReqRespVO> getMaterialReq(@RequestParam("id") Long id) {
        return success(materialReqService.getMaterialReq(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得领退补料申请分页")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqRespVO>> getMaterialReqPage(@Valid MaterialReqPageReqVO pageReqVO) {
        return success(materialReqService.getMaterialReqPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出领退补料申请 Excel")
    @PreAuthorize("@ss.hasPermission('mes:material-req:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaterialReqExcel(@Valid MaterialReqPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MaterialReqRespVO> list = materialReqService.getMaterialReqPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-领退补料申请.xls", "数据", MaterialReqRespVO.class, list);
    }

    // ==================== 子表（领退补料申请明细） ====================

    @GetMapping("/material-req-line/page")
    @Operation(summary = "获得领退补料申请明细分页")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqLineRespVO>> getMaterialReqLinePage(
            @Valid MaterialReqLinePageReqVO pageReqVO) {
        return success(materialReqService.getMaterialReqLinePage(pageReqVO));
    }

    @GetMapping("/material-req-line/get")
    @Operation(summary = "获得领退补料申请明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<MaterialReqLineRespVO> getMaterialReqLine(@RequestParam("id") Long id) {
        return success(materialReqService.getMaterialReqLine(id));
    }

    @PostMapping("/material-req-line/create")
    @Operation(summary = "创建领退补料申请明细，仅主单新建状态可加行")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReqLine(@Valid @RequestBody MaterialReqLineSaveReqVO createReqVO) {
        mesMaterialReqDomainService.validateReqDraft(createReqVO.getReqId());
        return success(materialReqService.createMaterialReqLine(createReqVO));
    }

    @PutMapping("/material-req-line/update")
    @Operation(summary = "更新领退补料申请明细，仅主单新建状态可改")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReqLine(@Valid @RequestBody MaterialReqLineSaveReqVO updateReqVO) {
        mesMaterialReqDomainService.validateReqDraft(updateReqVO.getReqId());
        materialReqService.updateMaterialReqLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete")
    @Operation(summary = "删除领退补料申请明细，仅主单新建状态可删")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLine(@RequestParam("id") Long id) {
        mesMaterialReqDomainService.validateLineDraft(List.of(id));
        materialReqService.deleteMaterialReqLine(id);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete-list")
    @Operation(summary = "批量删除领退补料申请明细，仅主单新建状态可删")
    @Parameter(name = "ids", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLineList(@RequestParam("ids") List<Long> ids) {
        mesMaterialReqDomainService.validateLineDraft(ids);
        materialReqService.deleteMaterialReqLineListByIds(ids);
        return success(true);
    }

    // ==================== 内部转换 ====================

    /** 提交与取消共用一个 BO：两个动作的入参完全相同，库存后果由动作名决定。 */
    private MesMaterialReqActionBO toActionBO(MaterialReqActionReqVO reqVO) {
        MesMaterialReqActionBO bo = new MesMaterialReqActionBO();
        bo.setMaterialReqId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return bo;
    }

    /**
     * 补齐命令的来源与幂等键。
     *
     * <p>来源模块固定 {@code UI}：端点是人在界面上点出来的，来源就是后台人工。
     *
     * <p>幂等键原样透传，<b>为空是常态</b>：领域 Service 会按 {@code UI:{申请单号}:{命令类型}:0}
     * （创建按 {@code UI:{工单号}:CREATE_REQ:0}）确定性派生。这三个动作对键的稳定性依赖最强 ——
     * 键不稳定，提交会占两笔预留、确认会过两笔账、取消会把可用量回升两次，等于凭空多出一批料。
     * 前端每次点击生成 uuid 或时间戳恰恰是最坏的做法。
     *
     * <p>来源单号留空：领域侧会兜底成单据编号，比数字主键在命令记录里更可读。
     */
    private void fillCommand(MesCommandReqDTO command, String idempotencyKey) {
        command.setSourceModule(MesSourceModuleEnum.UI.name());
        command.setIdempotencyKey(idempotencyKey);
    }

}
