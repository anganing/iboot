package com.iboot.pf.module.mes.controller.admin.trace.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批次追溯导出行 Response VO。
 *
 * <p>追溯链路是树，客户审核要交的是一张 Excel，因此把树按前序遍历拍平成行：{@code 层级} 列给出
 * 该行在链路中的深度（起点为 1），{@code 上级节点} 列给出父节点业务单号，两列配合可还原整棵树。
 * 九类节点的字段差异用固定列承载：物料 / 批次 / 数量 / 时间 / 状态 / 版本 / 供应商 / 质检 / 客户
 * 各占一列，某类节点没有的字段留空；{@code 关键快照} 列透出该节点全部快照键值，保证不丢信息。
 *
 * <p>枚举值一律在这行里已经翻译成中文（拍平在 Service 层完成），Excel 里不出现 {@code REJECTED}
 * 这类原始值。
 */
@Schema(description = "管理后台 - 批次追溯导出行 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TraceExcelRowRespVO {

    @Schema(description = "追溯方向（中文）", requiredMode = Schema.RequiredMode.REQUIRED, example = "正向")
    @ExcelProperty("追溯方向")
    private String directionName;

    @Schema(description = "起点类型（中文），只在首行给出", example = "批次")
    @ExcelProperty("起点类型")
    private String startTypeName;

    @Schema(description = "起点值，只在首行给出", example = "LOT20260801")
    @ExcelProperty("起点值")
    private String startValue;

    @Schema(description = "层级，起点为 1", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("层级")
    private Integer level;

    @Schema(description = "追溯路径：自起点到本节点的层级编号，如 1.2.3")
    @ExcelProperty("路径")
    private String path;

    @Schema(description = "上级节点业务单号", example = "MO20260901001")
    @ExcelProperty("上级节点")
    private String parentBizNo;

    @Schema(description = "节点类型（中文）", requiredMode = Schema.RequiredMode.REQUIRED, example = "批次")
    @ExcelProperty("节点类型")
    private String nodeTypeName;

    @Schema(description = "业务单号或批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT20260801")
    @ExcelProperty("业务单号")
    private String bizNo;

    @Schema(description = "节点名称", example = "钢板")
    @ExcelProperty("节点名称")
    private String nodeName;

    @Schema(description = "物料编码", example = "MAT001")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称", example = "冷轧钢板")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "批次号", example = "LOT20260801")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "数量", example = "100")
    @ExcelProperty("数量")
    private BigDecimal qty;

    @Schema(description = "节点时间（业务发生时间或实际完成时间）", example = "2026-09-01 10:00:00")
    @ExcelProperty("时间")
    private String bizTime;

    @Schema(description = "状态（中文）", example = "已过账")
    @ExcelProperty("状态")
    private String statusName;

    @Schema(description = "工序", example = "绕线")
    @ExcelProperty("工序")
    private String opName;

    @Schema(description = "客户名称", example = "宁波风电")
    @ExcelProperty("客户")
    private String customerName;

    @Schema(description = "供应商名称", example = "宝钢国贸")
    @ExcelProperty("供应商")
    private String supplierName;

    @Schema(description = "供应商批次", example = "SG-2026-08-001")
    @ExcelProperty("供应商批次")
    private String supplierLotNo;

    @Schema(description = "炉批号", example = "F-2026-08-001")
    @ExcelProperty("炉批号")
    private String furnaceLotNo;

    @Schema(description = "BOM 版本", example = "V1.2")
    @ExcelProperty("BOM版本")
    private String bomVersion;

    @Schema(description = "工艺版本", example = "R1.0")
    @ExcelProperty("工艺版本")
    private String routeVersion;

    @Schema(description = "质检触发点（中文）", example = "完工检验")
    @ExcelProperty("检验点")
    private String triggerPointName;

    @Schema(description = "质检结论（中文）", example = "合格")
    @ExcelProperty("质检结论")
    private String conclusionName;

    @Schema(description = "检验方案版本", example = "1")
    @ExcelProperty("检验方案版本")
    private String qcPlanVersion;

    @Schema(description = "是否因超过深度上限被截断", example = "否")
    @ExcelProperty("是否截断")
    private String truncatedName;

    @Schema(description = "关键快照全量键值，保证节点信息不丢", example = "orderNo=MO20260901001; qty=100")
    @ExcelProperty("关键快照")
    private String snapshotText;

}
