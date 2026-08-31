package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 质检触发规则 Response VO。
 *
 * <p>检验方案编码与名称来自模块内 JOIN；物料、工序、工作中心、供应商名称由 Service 调 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-质检触发规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcRuleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("规则编码")
    private String code;

    @Schema(description = "质检规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("规则名称")
    private String name;

    @Schema(description = "触发点", requiredMode = Schema.RequiredMode.REQUIRED, example = "IQC")
    @ExcelProperty(value = "触发点", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_TRIGGER_POINT)
    private String triggerPoint;

    @Schema(description = "检验对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MATERIAL")
    @ExcelProperty(value = "检验对象类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_OBJECT_TYPE)
    private String objectType;

    @Schema(description = "指定物料编号", example = "1")
    private Long matId;

    @Schema(description = "指定物料名称")
    @ExcelProperty("指定物料")
    private String matName;

    @Schema(description = "指定物料分类编号", example = "1")
    private Long matCatId;

    @Schema(description = "指定工序编号", example = "1")
    private Long opId;

    @Schema(description = "指定工序名称")
    @ExcelProperty("指定工序")
    private String opName;

    @Schema(description = "指定工作中心编号", example = "1")
    private Long wcId;

    @Schema(description = "指定工作中心名称")
    @ExcelProperty("指定工作中心")
    private String wcName;

    @Schema(description = "指定供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long qcPlanId;

    @Schema(description = "检验方案编码")
    @ExcelProperty("检验方案编码")
    private String qcPlanCode;

    @Schema(description = "检验方案名称")
    @ExcelProperty("检验方案名称")
    private String qcPlanName;

    @Schema(description = "检验模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROCESS")
    @ExcelProperty(value = "检验模式", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_MODE)
    private String qcMode;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
