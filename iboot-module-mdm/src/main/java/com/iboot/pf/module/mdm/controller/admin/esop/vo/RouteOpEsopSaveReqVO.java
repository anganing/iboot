package com.iboot.pf.module.mdm.controller.admin.esop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 工序 ESOP 作业步骤新增/修改 Request VO")
@Data
public class RouteOpEsopSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "路线工序不能为空")
    private Long routeOpId;

    @Schema(description = "展示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "展示顺序不能为空")
    @Min(value = 0, message = "展示顺序不能小于 0")
    @Max(value = 9999, message = "展示顺序不能大于 9999")
    private Integer seq;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "装配要点")
    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题长度不能超过 128")
    private String title;

    @Schema(description = "作业要点文本")
    private String content;

    @Schema(description = "工艺参数 JSON")
    private String paramJson;

    @Schema(description = "附件列表")
    @Valid
    @Size(max = 10, message = "附件数量不能超过 10 个")
    private List<RouteOpEsopFileReqVO> files;

    @Schema(description = "是否要求作业者确认阅读")
    private Boolean requiredConfirmFlag;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
