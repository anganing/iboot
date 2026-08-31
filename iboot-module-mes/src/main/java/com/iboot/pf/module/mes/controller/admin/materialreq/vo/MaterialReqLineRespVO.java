package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 领退补料申请明细 Response VO。物料、单位、批次字段都是写入时落库的快照。 */
@Schema(description = "管理后台 - MES-领退补料申请明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MaterialReqLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long reqId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "路线工序用料编号", example = "1")
    private Long routeOpMatId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("规格型号")
    private String matSpec;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("申请数量")
    private BigDecimal qty;

    @Schema(description = "WMS 已处理数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已处理数量")
    private BigDecimal processedQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "指定退料批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "是否必须在开工前发料", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "开工前发料", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BOOLEAN_STRING)
    private Boolean mustIssueBeforeStart;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "明细状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.MATERIAL_REQ_LINE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
