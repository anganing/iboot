package com.iboot.pf.module.wms.controller.admin.arrival.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-到货登记明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ArrivalLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "到货登记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long arrivalId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "来源单据行号")
    @ExcelProperty("来源单据行号")
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

    @Schema(description = "到货批次号")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "供应商批次号")
    @ExcelProperty("供应商批次号")
    private String supplierLotNo;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货数量")
    private BigDecimal arrivalQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "是否需要质检", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.BOOLEAN_STRING)
    private Boolean needQc;

    @Schema(description = "来料检验模式")
    @ExcelProperty(value = "来料检验模式", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.IQC_MODE)
    private String iqcMode;

    @Schema(description = "质检任务编号")
    private Long qcTaskId;

    @Schema(description = "合格数量")
    @ExcelProperty("合格数量")
    private BigDecimal qualifiedQty;

    @Schema(description = "不合格数量")
    @ExcelProperty("不合格数量")
    private BigDecimal rejectedQty;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "明细状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ARRIVAL_LINE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
