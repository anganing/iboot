package com.iboot.pf.module.wms.controller.admin.countorder;

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

import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.count.WmsCountOrderService;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.bo.WmsCreateCountOrderBO;
import com.iboot.pf.module.wms.service.count.bo.WmsSubmitCountResultBO;
import com.iboot.pf.module.wms.service.countorder.CountOrderService;
import com.iboot.pf.module.wms.service.order.bo.WmsSubmitOrderBO;

@Tag(name = "管理后台 - WMS-盘点单")
@RestController
@RequestMapping("/wms/count-order")
@Validated
public class CountOrderController {

    /** 领域 Service，承担全部写入与状态推进：库存余额只能经它改。 */
    @Resource
    private WmsCountOrderService wmsCountOrderService;
    /** codegen Service，只用于列表、详情与导出的 JOIN 展示查询。 */
    @Resource
    private CountOrderService countOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-盘点单，按盘点范围生成盘点行")
    @PreAuthorize("@ss.hasPermission('wms:count-order:create')")
    public CommonResult<Long> createCountOrder(@Valid @RequestBody CountOrderSaveReqVO createReqVO) {
        return success(wmsCountOrderService.createCountOrder(toCreateBO(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-盘点单，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('wms:count-order:update')")
    public CommonResult<Boolean> updateCountOrder(@Valid @RequestBody CountOrderSaveReqVO updateReqVO) {
        wmsCountOrderService.updateCountOrder(updateReqVO.getId(), toCreateBO(updateReqVO));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-盘点单，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrder(@RequestParam("id") Long id) {
        wmsCountOrderService.deleteCountOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-盘点单，仅新建状态可删")
    @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrderList(@RequestParam("ids") List<Long> ids) {
        // 领域侧只有单据级删除，每张单都要过新建状态校验，因此逐个调用
        ids.forEach(wmsCountOrderService::deleteCountOrder);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/freeze")
    @Operation(summary = "冻结WMS-盘点范围，逐行按账面数量做冻结过账")
    @PreAuthorize("@ss.hasPermission('wms:count-order:freeze')")
    public CommonResult<Boolean> freezeCountOrder(@Valid @RequestBody CountOrderActionReqVO reqVO) {
        WmsSubmitOrderBO bo = new WmsSubmitOrderBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.FREEZE_COUNT));
        wmsCountOrderService.freezeCountOrder(bo);
        return success(true);
    }

    @PostMapping("/submit-result")
    @Operation(summary = "提交WMS-盘点实盘结果，一次提交多行，差异数量由服务端计算")
    @PreAuthorize("@ss.hasPermission('wms:count-order:submit-result')")
    public CommonResult<Boolean> submitCountResult(@Valid @RequestBody CountOrderResultSubmitReqVO reqVO) {
        WmsSubmitCountResultBO bo = new WmsSubmitCountResultBO();
        bo.setOrderId(reqVO.getId());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.SUBMIT_COUNT_RESULT));
        bo.setResults(BeanUtils.toBean(reqVO.getResults(), WmsSubmitCountResultBO.LineResult.class));
        wmsCountOrderService.submitCountResult(bo);
        return success(true);
    }

    @PostMapping("/confirm-diff")
    @Operation(summary = "确认WMS-盘点差异，生成盘盈入库单或盘亏出库单并过账")
    @PreAuthorize("@ss.hasPermission('wms:count-order:confirm-diff')")
    public CommonResult<Boolean> confirmCountDiff(@Valid @RequestBody CountOrderActionReqVO reqVO) {
        WmsConfirmCountDiffBO bo = new WmsConfirmCountDiffBO();
        bo.setOrderId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CONFIRM_COUNT_DIFF));
        wmsCountOrderService.confirmCountDiff(bo);
        return success(true);
    }

    // ==================== 展示查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得WMS-盘点单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<CountOrderRespVO> getCountOrder(@RequestParam("id") Long id) {
        return success(countOrderService.getCountOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-盘点单分页")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<PageResult<CountOrderRespVO>> getCountOrderPage(@Valid CountOrderPageReqVO pageReqVO) {
        return success(countOrderService.getCountOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-盘点单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:count-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCountOrderExcel(@Valid CountOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CountOrderRespVO> list = countOrderService.getCountOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-盘点单.xls", "数据", CountOrderRespVO.class,
                        list);
    }

    // ==================== 子表（WMS-盘点明细） ====================
    // 只读，且这里是四类单据里最要紧的一处。明细的增删改已随调度方裁定一撤销：那四个端点走
    // codegen Service、完全不校验主单状态，能人工加盘点行、直接改 book_qty 与 actual_qty、
    // 删掉不想盘的行，冻结后与差异确认后都拦不住——而 book_qty 是账实对比的基准。
    // 盘点行只能由领域侧按余额快照生成，实盘录入只留 /submit-result 一条路。

    @GetMapping("/count-order-line/page")
    @Operation(summary = "获得WMS-盘点明细分页")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<PageResult<CountOrderLineRespVO>> getCountOrderLinePage(@Valid CountOrderLinePageReqVO pageReqVO) {
        return success(countOrderService.getCountOrderLinePage(pageReqVO));
    }

	@GetMapping("/count-order-line/get")
	@Operation(summary = "获得WMS-盘点明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
	public CommonResult<CountOrderLineRespVO> getCountOrderLine(@RequestParam("id") Long id) {
	    return success(countOrderService.getCountOrderLine(id));
	}

    // ==================== 内部转换 ====================

    /**
     * 请求 VO 转创建 BO。创建与修改共用：改盘点范围后旧行整批删除、按新范围重新生成，字段集与创建时相同。
     *
     * <p>{@code BeanUtils} 自动映射覆盖不到、必须手工补的字段：
     * <ol>
     *     <li>{@code sourceModule}：BO 里没有对应的 VO 字段，界面发起的单据一律记 UI；</li>
     *     <li>{@code matIds}：VO 侧是 {@code List}、BO 侧是 {@code Collection}，显式赋值避免依赖集合类型转换；</li>
     *     <li>VO 的 {@code status} 与 {@code snapshotTime} 不参与转换：状态由领域 Service 写死为新建，
     *     账面快照时间由它按生成盘点行的时刻重取。</li>
     * </ol>
     */
    private WmsCreateCountOrderBO toCreateBO(CountOrderSaveReqVO reqVO) {
        WmsCreateCountOrderBO bo = BeanUtils.toBean(reqVO, WmsCreateCountOrderBO.class);
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setMatIds(reqVO.getMatIds());
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