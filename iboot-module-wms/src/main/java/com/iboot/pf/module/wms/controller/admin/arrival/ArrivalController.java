package com.iboot.pf.module.wms.controller.admin.arrival;

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

import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.arrival.ArrivalService;
import com.iboot.pf.module.wms.service.arrivalorder.WmsArrivalOrderService;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsConfirmArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsCreateArrivalBO;
import com.iboot.pf.module.wms.service.arrivalorder.bo.WmsUpdateArrivalBO;

@Tag(name = "管理后台 - WMS-到货登记")
@RestController
@RequestMapping("/wms/arrival")
@Validated
public class ArrivalController {

    /** 领域 Service，承担全部写入与状态推进：批次注册与生成入库单只能经它做。 */
    @Resource
    private WmsArrivalOrderService wmsArrivalOrderService;
    /** codegen Service，只用于列表、详情与导出的 JOIN 展示查询。 */
    @Resource
    private ArrivalService arrivalService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-到货登记")
    @PreAuthorize("@ss.hasPermission('wms:arrival:create')")
    public CommonResult<Long> createArrival(@Valid @RequestBody ArrivalSaveReqVO createReqVO) {
        return success(wmsArrivalOrderService.createArrival(toCreateBO(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-到货登记，仅新建状态可改")
    @PreAuthorize("@ss.hasPermission('wms:arrival:update')")
    public CommonResult<Boolean> updateArrival(@Valid @RequestBody ArrivalSaveReqVO updateReqVO) {
        wmsArrivalOrderService.updateArrival(toUpdateBO(updateReqVO));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-到货登记，仅新建状态可删")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrival(@RequestParam("id") Long id) {
        wmsArrivalOrderService.deleteArrival(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-到货登记，仅新建状态可删")
    @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrivalList(@RequestParam("ids") List<Long> ids) {
        // 领域侧只有单据级删除，每张单都要过新建状态校验，因此逐个调用
        ids.forEach(wmsArrivalOrderService::deleteArrival);
        return success(true);
    }

    // ==================== 领域动作 ====================

    @PostMapping("/confirm")
    @Operation(summary = "确认到货，注册批次并按是否需要质检给明细分流")
    @PreAuthorize("@ss.hasPermission('wms:arrival:confirm')")
    public CommonResult<Boolean> confirmArrival(@Valid @RequestBody ArrivalActionReqVO reqVO) {
        WmsConfirmArrivalBO bo = new WmsConfirmArrivalBO();
        bo.setArrivalId(reqVO.getId());
        bo.setArrivalTime(reqVO.getArrivalTime());
        bo.setRemark(reqVO.getRemark());
        bo.setSourceModule(WmsSourceModuleEnum.UI.name());
        bo.setSourceBizNo(String.valueOf(reqVO.getId()));
        bo.setIdempotencyKey(actionKey(reqVO.getIdempotencyKey(), reqVO.getId(),
                WmsCommandTypeEnum.CONFIRM_ARRIVAL));
        wmsArrivalOrderService.confirmArrival(bo);
        return success(true);
    }

    @PostMapping("/create-in-order")
    @Operation(summary = "按待入库明细生成采购入库单，明细编号留空表示整单生成")
    @PreAuthorize("@ss.hasPermission('wms:arrival:create-in-order')")
    public CommonResult<Long> createInOrderFromArrival(@Valid @RequestBody ArrivalInOrderCreateReqVO reqVO) {
        // 幂等键由领域侧按「到货单号 + 本次选中的行号串」派生，见 ArrivalInOrderCreateReqVO 的类注释
        return success(wmsArrivalOrderService.createInOrderFromArrival(reqVO.getId(), reqVO.getLineIds()));
    }

    // ==================== 展示查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得WMS-到货登记")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<ArrivalRespVO> getArrival(@RequestParam("id") Long id) {
        return success(arrivalService.getArrival(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-到货登记分页")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<PageResult<ArrivalRespVO>> getArrivalPage(@Valid ArrivalPageReqVO pageReqVO) {
        return success(arrivalService.getArrivalPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-到货登记 Excel")
    @PreAuthorize("@ss.hasPermission('wms:arrival:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArrivalExcel(@Valid ArrivalPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ArrivalRespVO> list = arrivalService.getArrivalPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-到货登记.xls", "数据", ArrivalRespVO.class,
                        list);
    }

    // ==================== 子表（WMS-到货登记明细） ====================
    // 只读。明细的增删改已按调度方裁定一撤销：那四个端点走 codegen Service、完全不校验主单状态，
    // 已确认到货（批次已注册）甚至已生成入库单的到货单也能加行改数量。新建态的明细编辑走主单
    // /update，领域侧是"旧行整批删除后重建"语义；非新建态的明细本就不该能改。

    @GetMapping("/arrival-line/page")
    @Operation(summary = "获得WMS-到货登记明细分页")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<PageResult<ArrivalLineRespVO>> getArrivalLinePage(@Valid ArrivalLinePageReqVO pageReqVO) {
        return success(arrivalService.getArrivalLinePage(pageReqVO));
    }

	@GetMapping("/arrival-line/get")
	@Operation(summary = "获得WMS-到货登记明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
	public CommonResult<ArrivalLineRespVO> getArrivalLine(@RequestParam("id") Long id) {
	    return success(arrivalService.getArrivalLine(id));
	}

    // ==================== 内部转换 ====================

    /**
     * 请求 VO 转创建 BO。
     *
     * <p>与入库、出库、转移三个单据不同，这里**不需要**手工置 {@code sourceModule}：到货登记 BO 里没有
     * 这个字段，它的 {@code sourceType} 是业务来源类型（采购订单 / ASN / 销售退货），与 VO 同名同义直接流转。
     *
     * <p>要手工补的只有 {@code lines}：嵌套列表显式逐元素转换，不依赖 hutool 对集合元素类型的推导。
     * VO 的 {@code status} 与 {@code arrivalTime} 不参与转换：BO 里没有这两个字段，
     * 单据状态由领域 Service 写死为新建，实际到货时间是确认到货动作的留痕。
     */
    private WmsCreateArrivalBO toCreateBO(ArrivalSaveReqVO reqVO) {
        WmsCreateArrivalBO bo = BeanUtils.toBean(reqVO, WmsCreateArrivalBO.class);
        bo.setLines(BeanUtils.toBean(reqVO.getLines(), WmsCreateArrivalBO.Line.class));
        return bo;
    }

    /**
     * 请求 VO 转改单 BO。改单在语义上就是按新内容整体替换，字段集与创建时相同，所以
     * {@code WmsUpdateArrivalBO} 直接继承创建 BO、只多一个目标编号。
     *
     * <p>那个编号必须显式补：VO 侧叫 {@code id}、BO 侧叫 {@code arrivalId}，名字对不上，
     * {@code BeanUtils} 按同名属性拷贝，漏了它领域侧会当作"单据不存在"抛错。
     */
    private WmsUpdateArrivalBO toUpdateBO(ArrivalSaveReqVO reqVO) {
        WmsUpdateArrivalBO bo = BeanUtils.toBean(reqVO, WmsUpdateArrivalBO.class);
        bo.setLines(BeanUtils.toBean(reqVO.getLines(), WmsCreateArrivalBO.Line.class));
        bo.setArrivalId(reqVO.getId());
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
