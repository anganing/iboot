package com.iboot.pf.module.wms.controller.admin.outordermgr;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.CommonResult;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;
import com.iboot.pf.module.wms.service.outorder.WmsOutOrderService;
import com.iboot.pf.module.wms.service.outorder.bo.WmsCreateOutOrderBO;
import com.iboot.pf.module.wms.service.outordermgr.OutOrderService;

@Tag(name = "管理后台 - WMS-出库单")
@RestController
@RequestMapping("/wms/out-order")
@Validated
public class OutOrderController {

    /** 领域 Service，承担全部写入与状态推进：库存余额只能经它改。 */
    @Resource
    private WmsOutOrderService wmsOutOrderService;
    /** codegen Service，只用于列表、详情与导出的 JOIN 展示查询。 */
    @Resource
    private OutOrderService outOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-出库单")
    @PreAuthorize("@ss.hasPermission('wms:out-order:create')")
    public CommonResult<Long> createOutOrder(@Valid @RequestBody OutOrderSaveReqVO createReqVO) {
        return success(wmsOutOrderService.createOutOrder(toCreateBO(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-出库单，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('wms:out-order:update')")
    public CommonResult<Boolean> updateOutOrder(@Valid @RequestBody OutOrderSaveReqVO updateReqVO) {
        wmsOutOrderService.updateOutOrder(updateReqVO.getId(), toCreateBO(updateReqVO));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-出库单，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrder(@RequestParam("id") Long id) {
        wmsOutOrderService.deleteOutOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-出库单，仅新建状态可删")
    @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrderList(@RequestParam("ids") List<Long> ids) {
        // 领域侧只有单据级删除，每张单都要过新建状态校验，因此逐个调用
        ids.forEach(wmsOutOrderService::deleteOutOrder);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/submit")
    @Operation(summary = "提交WMS-出库单，新建推进到已提交")
    @PreAuthorize("@ss.hasPermission('wms:out-order:submit')")
    public CommonResult<Boolean> submitOutOrder(@Valid @RequestBody OutOrderActionReqVO reqVO) {
        WmsSubmitOrderBO bo = new WmsSubmitOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.SUBMIT_OUT_ORDER));
        wmsOutOrderService.submitOutOrder(bo);
        return success(true);
    }

    @PostMapping("/confirm")
    @Operation(summary = "过账WMS-出库单，写库存流水、扣减余额并核销行上预留")
    @PreAuthorize("@ss.hasPermission('wms:out-order:confirm')")
    public CommonResult<Boolean> confirmOutOrder(@Valid @RequestBody OutOrderActionReqVO reqVO) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CONFIRM_OUT_ORDER));
        wmsOutOrderService.confirmOutOrder(bo);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消WMS-出库单，仅新建状态可取消")
    @PreAuthorize("@ss.hasPermission('wms:out-order:cancel')")
    public CommonResult<Boolean> cancelOutOrder(@Valid @RequestBody OutOrderCancelReqVO reqVO) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CANCEL_OUT_ORDER));
        wmsOutOrderService.cancelOutOrder(bo);
        return success(true);
    }

    // ==================== 展示查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得WMS-出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<OutOrderRespVO> getOutOrder(@RequestParam("id") Long id) {
        return success(outOrderService.getOutOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-出库单分页")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<PageResult<OutOrderRespVO>> getOutOrderPage(@Valid OutOrderPageReqVO pageReqVO) {
        return success(outOrderService.getOutOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-出库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:out-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutOrderExcel(@Valid OutOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OutOrderRespVO> list = outOrderService.getOutOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-出库单.xls", "数据", OutOrderRespVO.class,
                        list);
    }

    // ==================== 子表（WMS-出库单明细） ====================
    // 只读。明细的增删改已随调度方裁定一撤销：那四个端点走 codegen Service、完全不校验主单状态，
    // 已过账的单据也能加行改数量。新建态的明细编辑走主单 /update，领域侧 insertLines 是
    // "旧行整批删除后重建"语义，行号连续性与单头汇总都由它维护；非新建态的明细本就不该能改。

    @GetMapping("/out-order-line/page")
    @Operation(summary = "获得WMS-出库单明细分页")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<PageResult<OutOrderLineRespVO>> getOutOrderLinePage(@Valid OutOrderLinePageReqVO pageReqVO) {
        return success(outOrderService.getOutOrderLinePage(pageReqVO));
    }

	@GetMapping("/out-order-line/get")
	@Operation(summary = "获得WMS-出库单明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
	public CommonResult<OutOrderLineRespVO> getOutOrderLine(@RequestParam("id") Long id) {
	    return success(outOrderService.getOutOrderLine(id));
	}

    // ==================== 内部转换 ====================

    /**
     * 请求 VO 转创建 BO。创建与修改共用：修改在语义上就是按新内容整体替换，字段集与创建时相同。
     *
     * <p>三处 {@code BeanUtils} 自动映射覆盖不到、必须手工补的字段：
     * <ol>
     *     <li>{@code sourceModule}：VO 侧的字段名是 {@code sourceType}，且界面发起的单据一律记 UI，不听前端的；</li>
     *     <li>{@code lines}：嵌套列表显式逐元素转换，不依赖 hutool 对集合元素类型的推导；</li>
     *     <li>VO 的 {@code status} 不参与转换：BO 里没有这个字段，单据状态由领域 Service 写死为新建。</li>
     * </ol>
     */
    private WmsCreateOutOrderBO toCreateBO(OutOrderSaveReqVO reqVO) {
        WmsCreateOutOrderBO bo = BeanUtils.toBean(reqVO, WmsCreateOutOrderBO.class);
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setLines(BeanUtils.toBean(reqVO.getLines(), WmsCreateOutOrderBO.Line.class));
        return bo;
    }

    /**
     * 派生动作幂等键，格式 {@code sourceModule:sourceBizNo:action:sourceBizLineNo}，整单动作行号段固定为 0。
     *
     * <p>前端传了就用前端的；没传按单据编号与命令类型拼。这是确定性派生而不是随机键：
     * 同一张单据的同一个动作恒为同一个键，界面连点两次、浏览器重发都会被
     * {@code wms_command_record} 的唯一索引判重。服务端若生成随机键，两次点击拿到两个不同的键，
     * 幂等就完全失效了。
     *
     * <p>过账动作还要靠它派生每行的库存流水键与预留核销键，键不稳定会把同一行的余额扣两遍。
     */
    private String actionKey(String customKey, Long orderId, WmsCommandTypeEnum commandType) {
        if (customKey != null && !customKey.isEmpty()) {
            return customKey;
        }
        return String.format("%s:%d:%s:0", WmsSourceModuleEnum.UI.name(), orderId, commandType.name());
    }

}
