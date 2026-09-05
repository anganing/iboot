package com.iboot.pf.module.wms.controller.admin.inordermgr;

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

import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
import com.iboot.pf.module.wms.service.inorder.bo.WmsCreateInOrderBO;
import com.iboot.pf.module.wms.service.inordermgr.InOrderService;
import com.iboot.pf.module.wms.service.order.bo.WmsCancelOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsConfirmOrderBO;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;

@Tag(name = "管理后台 - WMS-入库单")
@RestController
@RequestMapping("/wms/in-order")
@Validated
public class InOrderController {

    /** 领域 Service，承担全部写入与状态推进：库存余额只能经它改。 */
    @Resource
    private WmsInOrderService wmsInOrderService;
    /** codegen Service，只用于列表、详情与导出的 JOIN 展示查询。 */
    @Resource
    private InOrderService inOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-入库单")
    @PreAuthorize("@ss.hasPermission('wms:in-order:create')")
    public CommonResult<Long> createInOrder(@Valid @RequestBody InOrderSaveReqVO createReqVO) {
        return success(wmsInOrderService.createInOrder(toCreateBO(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-入库单，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('wms:in-order:update')")
    public CommonResult<Boolean> updateInOrder(@Valid @RequestBody InOrderSaveReqVO updateReqVO) {
        wmsInOrderService.updateInOrder(updateReqVO.getId(), toCreateBO(updateReqVO));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-入库单，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrder(@RequestParam("id") Long id) {
        wmsInOrderService.deleteInOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-入库单，仅新建状态可删")
    @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrderList(@RequestParam("ids") List<Long> ids) {
        // 领域侧只有单据级删除，每张单都要过新建状态校验，因此逐个调用
        ids.forEach(wmsInOrderService::deleteInOrder);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/submit")
    @Operation(summary = "提交WMS-入库单，新建推进到已确认")
    @PreAuthorize("@ss.hasPermission('wms:in-order:submit')")
    public CommonResult<Boolean> submitInOrder(@Valid @RequestBody InOrderActionReqVO reqVO) {
        WmsSubmitOrderBO bo = new WmsSubmitOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.SUBMIT_IN_ORDER));
        wmsInOrderService.submitInOrder(bo);
        return success(true);
    }

    @PostMapping("/confirm")
    @Operation(summary = "过账WMS-入库单，写库存流水并增加余额")
    @PreAuthorize("@ss.hasPermission('wms:in-order:confirm')")
    public CommonResult<Boolean> confirmInOrder(@Valid @RequestBody InOrderActionReqVO reqVO) {
        WmsConfirmOrderBO bo = new WmsConfirmOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CONFIRM_IN_ORDER));
        wmsInOrderService.confirmInOrder(bo);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消WMS-入库单，仅新建状态可取消")
    @PreAuthorize("@ss.hasPermission('wms:in-order:cancel')")
    public CommonResult<Boolean> cancelInOrder(@Valid @RequestBody InOrderCancelReqVO reqVO) {
        WmsCancelOrderBO bo = new WmsCancelOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CANCEL_IN_ORDER));
        wmsInOrderService.cancelInOrder(bo);
        return success(true);
    }

    // ==================== 展示查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得WMS-入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<InOrderRespVO> getInOrder(@RequestParam("id") Long id) {
        return success(inOrderService.getInOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-入库单分页")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<PageResult<InOrderRespVO>> getInOrderPage(@Valid InOrderPageReqVO pageReqVO) {
        return success(inOrderService.getInOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-入库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:in-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInOrderExcel(@Valid InOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InOrderRespVO> list = inOrderService.getInOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-入库单.xls", "数据", InOrderRespVO.class,
                        list);
    }

    // ==================== 子表（WMS-入库单明细） ====================
    // 只读。明细的增删改已随调度方裁定一撤销：那四个端点走 codegen Service、完全不校验主单状态，
    // 已过账的单据也能加行改数量。新建态的明细编辑走主单 /update，领域侧 insertLines 是
    // "旧行整批删除后重建"语义，行号连续性与单头汇总都由它维护；非新建态的明细本就不该能改。

    @GetMapping("/in-order-line/page")
    @Operation(summary = "获得WMS-入库单明细分页")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<PageResult<InOrderLineRespVO>> getInOrderLinePage(@Valid InOrderLinePageReqVO pageReqVO) {
        return success(inOrderService.getInOrderLinePage(pageReqVO));
    }

	@GetMapping("/in-order-line/get")
	@Operation(summary = "获得WMS-入库单明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
	public CommonResult<InOrderLineRespVO> getInOrderLine(@RequestParam("id") Long id) {
	    return success(inOrderService.getInOrderLine(id));
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
    private WmsCreateInOrderBO toCreateBO(InOrderSaveReqVO reqVO) {
        WmsCreateInOrderBO bo = BeanUtils.toBean(reqVO, WmsCreateInOrderBO.class);
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setLines(BeanUtils.toBean(reqVO.getLines(), WmsCreateInOrderBO.Line.class));
        return bo;
    }

    /**
     * 派生动作幂等键，格式 {@code sourceModule:sourceBizNo:action:sourceBizLineNo}，整单动作行号段固定为 0。
     *
     * <p>前端传了就用前端的；没传按单据编号与命令类型拼。这是确定性派生而不是随机键：
     * 同一张单据的同一个动作恒为同一个键，界面连点两次、浏览器重发都会被
     * {@code wms_command_record} 的唯一索引判重。服务端若生成随机键，两次点击拿到两个不同的键，
     * 幂等就完全失效了。
     */
    private String actionKey(String customKey, Long orderId, WmsCommandTypeEnum commandType) {
        if (customKey != null && !customKey.isEmpty()) {
            return customKey;
        }
        return String.format("%s:%d:%s:0", WmsSourceModuleEnum.UI.name(), orderId, commandType.name());
    }

}