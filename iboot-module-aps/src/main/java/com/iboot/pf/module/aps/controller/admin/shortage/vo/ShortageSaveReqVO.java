package com.iboot.pf.module.aps.controller.admin.shortage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-排程缺料清单新增/修改 Request VO")
@Data
public class ShortageSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24791")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32435")
    @NotNull(message = "排程版本编号不能为空")
    private Long versionId;

    @Schema(description = "生产工单编号，0表示汇总缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "22727")
    @NotNull(message = "生产工单编号，0表示汇总缺料不能为空")
    private Long prodOrderId;

    @Schema(description = "工序任务编号，0表示非工序级缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "23255")
    @NotNull(message = "工序任务编号，0表示非工序级缺料不能为空")
    private Long opTaskId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "22319")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "3778")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "短缺物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8489")
    @NotNull(message = "短缺物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求数量不能为空")
    private BigDecimal reqQty;

    @Schema(description = "可用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用数量不能为空")
    private BigDecimal availQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "短缺数量不能为空")
    private BigDecimal shortQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16732")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "计量单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "需求到位时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求到位时间不能为空")
    private LocalDateTime needDate;

    @Schema(description = "预计供给时间")
    private LocalDateTime expectedSupplyDate;

    @Schema(description = "状态：OPEN、PARTIAL、RESOLVED、IGNORED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：OPEN、PARTIAL、RESOLVED、IGNORED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}