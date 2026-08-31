package com.iboot.pf.module.wms.controller.admin.wave.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-波次明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WaveLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "波次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long waveId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long outOrderId;

    @Schema(description = "出库单号")
    @ExcelProperty("出库单号")
    private String outOrderNo;

    @Schema(description = "出库单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long outOrderLineId;

    @Schema(description = "出库单明细行号")
    @ExcelProperty("出库行号")
    private Integer outOrderLineNo;

    @Schema(description = "出库明细物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "出库明细物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "出库明细计划数量")
    @ExcelProperty("计划出库数量")
    private BigDecimal planQty;

    @Schema(description = "波次内优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "明细状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.WAVE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
