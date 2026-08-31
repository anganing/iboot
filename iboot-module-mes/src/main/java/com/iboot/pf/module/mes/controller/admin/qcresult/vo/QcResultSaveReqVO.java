package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检验结果新增/修改 Request VO。
 *
 * <p>检验项编码与名称是快照，由后端从检验项目带出；结论与是否合格标记必须一致，由 Service 校验。
 */
@Schema(description = "管理后台 - MES-检验结果新增/修改 Request VO")
@Data
public class QcResultSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验项目编号不能为空")
    private Long itemId;

    @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "样本序号不能为空")
    @Min(value = 1, message = "样本序号必须大于零")
    private Integer sampleNo;

    @Schema(description = "实测值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "实测值不能为空")
    @Size(max = 255, message = "实测值长度不能超过 255")
    private String actualValue;

    @Schema(description = "是否合格", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否合格不能为空")
    private Boolean passFlag;

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验员编号不能为空")
    private Long inspectorId;

    @Schema(description = "检验时间，留空取当前时间")
    private LocalDateTime inspectTime;

    @Schema(description = "本项判定合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "本项判定合格数量不能为空")
    @DecimalMin(value = "0", message = "合格数量不能为负数")
    private BigDecimal qtyQualified;

    @Schema(description = "本项判定不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "本项判定不合格数量不能为空")
    @DecimalMin(value = "0", message = "不合格数量不能为负数")
    private BigDecimal qtyRejected;

    @Schema(description = "项目结论", requiredMode = Schema.RequiredMode.REQUIRED, example = "PASS")
    @NotEmpty(message = "项目结论不能为空")
    @InEnum(MesQcItemConclusionEnum.class)
    private String conclusion;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
