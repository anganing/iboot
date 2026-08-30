package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 排程场景新增/修改 Request VO")
@Data
public class ScenarioSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3976")
    private Long id;

    @Schema(description = "排程场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排程场景编码不能为空")
    private String code;

    @Schema(description = "排程场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "排程场景名称不能为空")
    private String name;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20450")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号，0表示全工厂", requiredMode = Schema.RequiredMode.REQUIRED, example = "14740")
    @NotNull(message = "车间编号，0表示全工厂不能为空")
    private Long workshopId;

    @Schema(description = "排程策略编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18129")
    @NotNull(message = "排程策略编号不能为空")
    private Long strategyId;

    @Schema(description = "计划窗口开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划窗口开始时间不能为空")
    private LocalDateTime planStartTime;

    @Schema(description = "计划窗口结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划窗口结束时间不能为空")
    private LocalDateTime planEndTime;

    @Schema(description = "输入数据快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED不能为空")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}