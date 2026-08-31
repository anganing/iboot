package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程场景新增/修改 Request VO。
 *
 * <p>输入数据快照时间由投影刷新写入，不由前端提交。
 */
@Schema(description = "管理后台 - APS-排程场景新增/修改 Request VO")
@Data
public class ScenarioSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "排程场景编码，留空由编码规则分配")
    @Size(max = 64, message = "排程场景编码长度不能超过 64")
    private String code;

    @Schema(description = "排程场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排程场景名称不能为空")
    @Size(max = 128, message = "排程场景名称长度不能超过 128")
    private String name;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号，0 表示全工厂", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "排程策略编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排程策略编号不能为空")
    private Long strategyId;

    @Schema(description = "计划窗口开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划窗口开始时间不能为空")
    private LocalDateTime planStartTime;

    @Schema(description = "计划窗口结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划窗口结束时间不能为空")
    private LocalDateTime planEndTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(ApsScenarioStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
