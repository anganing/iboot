package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-检验结果新增/修改 Request VO")
@Data
public class QcResultSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "890")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1159")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25386")
    @NotNull(message = "检验项目编号不能为空")
    private Long itemId;

    @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "样本序号不能为空")
    private Integer sampleNo;

    @Schema(description = "检验项编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验项编码快照不能为空")
    private String itemCode;

    @Schema(description = "检验项名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "检验项名称快照不能为空")
    private String itemName;

    @Schema(description = "实测值")
    private String actualValue;

    @Schema(description = "是否合格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否合格不能为空")
    private Boolean passFlag;

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29231")
    @NotNull(message = "检验员编号不能为空")
    private Long inspectorId;

    @Schema(description = "检验时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "检验时间不能为空")
    private LocalDateTime inspectTime;

    @Schema(description = "本项判定合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本项判定合格数量不能为空")
    private BigDecimal qtyQualified;

    @Schema(description = "本项判定不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本项判定不合格数量不能为空")
    private BigDecimal qtyRejected;

    @Schema(description = "项目结论：PASS、FAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "项目结论：PASS、FAIL不能为空")
    private String conclusion;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}