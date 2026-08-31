package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 检验方案 Response VO。 */
@Schema(description = "管理后台 - MES-检验方案 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcPlanRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "检验方案编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("方案编码")
    private String code;

    @Schema(description = "检验方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("方案名称")
    private String name;

    @Schema(description = "方案版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    @ExcelProperty("版本号")
    private String versionNo;

    @Schema(description = "抽样类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "FULL")
    @ExcelProperty(value = "抽样类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_SAMPLE_TYPE)
    private String sampleType;

    @Schema(description = "固定数量或抽样比例", example = "1")
    @ExcelProperty("抽样值")
    private BigDecimal sampleValue;

    @Schema(description = "AQL 接收质量限")
    @ExcelProperty("AQL")
    private String aql;

    @Schema(description = "接收判定规则")
    @ExcelProperty("接收判定规则")
    private String acceptanceRule;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_PLAN_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
