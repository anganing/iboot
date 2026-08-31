package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-库存转移调拨明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TransferOrderLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "转移调拨单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "容器编号，0表示散件", example = "0")
    private Long containerId;

    @Schema(description = "容器编号快照")
    @ExcelProperty("容器编号")
    private String containerNo;

    @Schema(description = "来源库位编号", example = "1")
    private Long fromLocId;

    @Schema(description = "来源库位编码")
    @ExcelProperty("来源库位")
    private String fromLocCode;

    @Schema(description = "目标库位编号", example = "1")
    private Long toLocId;

    @Schema(description = "目标库位编码")
    @ExcelProperty("目标库位")
    private String toLocCode;

    @Schema(description = "来源库存范围", example = "COMMON")
    @ExcelProperty(value = "来源库存范围", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.STOCK_SCOPE)
    private String fromStockScope;

    @Schema(description = "目标库存范围", example = "COMMON")
    @ExcelProperty(value = "目标库存范围", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.STOCK_SCOPE)
    private String toStockScope;

    @Schema(description = "来源项目编号，0表示公共库存", example = "0")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，0表示公共库存", example = "0")
    private Long toProjectId;

    @Schema(description = "来源项目任务编号，0表示无项目任务", example = "0")
    private Long fromProjectTaskId;

    @Schema(description = "目标项目任务编号，0表示无项目任务", example = "0")
    private Long toProjectTaskId;

    @Schema(description = "来源质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @ExcelProperty(value = "来源质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatusFrom;

    @Schema(description = "目标质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @ExcelProperty(value = "目标质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatusTo;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "基准单位编码快照")
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "计划转移数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划数量")
    private BigDecimal planQty;

    @Schema(description = "已搬运数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已搬运数量")
    private BigDecimal movedQty;

    @Schema(description = "已过账数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已过账数量")
    private BigDecimal postedQty;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "明细状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ORDER_LINE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
