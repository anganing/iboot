package com.iboot.pf.module.mes.controller.admin.rework.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 返工任务 Response VO。单号类字段来自模块内 JOIN。 */
@Schema(description = "管理后台 - MES-返工任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ReworkRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "返工任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("返工任务编号")
    private String reworkNo;

    @Schema(description = "来源质检任务编号", example = "1")
    private Long sourceQcTaskId;

    @Schema(description = "来源质检任务号")
    @ExcelProperty("来源质检任务号")
    private String sourceQcTaskNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "返工起始工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "返工起始工序任务号")
    @ExcelProperty("起始工序任务号")
    private String opTaskNo;

    @Schema(description = "返工起始工序名称")
    @ExcelProperty("起始工序")
    private String opName;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("返工数量")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeId;

    @Schema(description = "返工工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工艺路线版本")
    private String routeVersion;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.REWORK_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
