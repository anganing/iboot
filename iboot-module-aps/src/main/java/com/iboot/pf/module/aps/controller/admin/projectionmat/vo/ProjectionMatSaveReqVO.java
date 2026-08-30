package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-MDM物料投影新增/修改 Request VO")
@Data
public class ProjectionMatSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2416")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10501")
    @NotNull(message = "排程场景编号不能为空")
    private Long scenarioId;

    @Schema(description = "MDM物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19796")
    @NotNull(message = "MDM物料编号不能为空")
    private Long sourceMatId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "物料类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "物料类型快照不能为空")
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21854")
    @NotNull(message = "基准计量单位编号不能为空")
    private Long baseUomId;

    @Schema(description = "基准计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "基准计量单位编码快照不能为空")
    private String baseUomCode;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否批次管控不能为空")
    private Boolean batchFlag;

    @Schema(description = "当前有效BOM编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "22653")
    @NotNull(message = "当前有效BOM编号，0表示无不能为空")
    private Long bomId;

    @Schema(description = "当前有效BOM版本")
    private String bomVersion;

    @Schema(description = "当前有效工艺路线编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "25881")
    @NotNull(message = "当前有效工艺路线编号，0表示无不能为空")
    private Long routeId;

    @Schema(description = "当前有效工艺路线版本")
    private String routeVersion;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源数据更新时间不能为空")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投影快照时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}