package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 检验结果批量录入请求。 */
@Schema(description = "管理后台 - MES-检验结果批量录入 Request VO")
@Data
public class QcResultBatchSaveReqVO {

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验员不能为空")
    private Long inspectorId;

    @Schema(description = "检验结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "检验结果不能为空")
    private List<Item> items;

    /** 单格检验结果。 */
    @Schema(description = "管理后台 - MES-检验结果格子 Request VO")
    @Data
    public static class Item {

        @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "检验项目不能为空")
        private Long itemId;

        @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "样本序号不能为空")
        private Integer sampleNo;

        @Schema(description = "实测值", requiredMode = Schema.RequiredMode.REQUIRED)
        private String actualValue;
    }
}
