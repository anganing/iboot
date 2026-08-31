package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-入库单明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InOrderLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "来源明细编号", example = "1")
    private Long sourceLineId;

    @Schema(description = "来源明细行号")
    @ExcelProperty("来源明细行号")
    private String sourceLineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("规格型号")
    private String matSpec;

    @Schema(description = "批次编号，过账前可为空", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "目标库位编号", example = "1")
    private Long locId;

    @Schema(description = "目标库位编码快照")
    @ExcelProperty("目标库位")
    private String locCode;

    @Schema(description = "容器编号", example = "1")
    private Long containerId;

    @Schema(description = "容器编号快照")
    @ExcelProperty("容器编号")
    private String containerNo;

    @Schema(description = "入库质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @ExcelProperty(value = "质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatus;

    @Schema(description = "计划入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划数量")
    private BigDecimal planQty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "已过账数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已过账数量")
    private BigDecimal postedQty;

    @Schema(description = "质检任务编号", example = "1")
    private Long qcTaskId;

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
