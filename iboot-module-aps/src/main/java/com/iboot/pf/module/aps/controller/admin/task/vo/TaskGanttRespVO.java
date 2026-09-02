package com.iboot.pf.module.aps.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 排程甘特图 Response VO。
 *
 * <p>一次返回甘特图渲染需要的全部数据：任务条、两套分组元数据、任务之间的依赖连线、时间窗。
 * 不复用 {@code /aps/task/page}：甘特图要的是整版本的完整视图，分页拉回来的片段既画不出连线，
 * 也算不出时间轴范围。
 *
 * <p>任务平铺、分组元数据单独给：甘特图要支持资源维度与工单维度两种看法，前者回答"每台设备什么时候
 * 在干什么、哪台是瓶颈"，后者回答"这张工单什么时候能交、卡在哪道工序"。把任务嵌在分组里会把维度焊死
 * 在后端，换维度就得让前端先摊平再重组；平铺加两套分组键，前端按选中维度组装即可，一次请求覆盖两种视图。
 */
@Schema(description = "管理后台 - APS-排程甘特图 Response VO")
@Data
public class TaskGanttRespVO {

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号", example = "1")
    private Integer versionNo;

    @Schema(description = "排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    private String scenarioName;

    @Schema(description = "版本状态", example = "SOLVED")
    private String versionStatus;

    @Schema(description = "计划最早开始时间，时间轴左边界")
    private LocalDateTime minTime;

    @Schema(description = "计划最晚结束时间，时间轴右边界")
    private LocalDateTime maxTime;

    @Schema(description = "任务总条数", example = "128")
    private Integer taskCount;

    @Schema(description = "任务条，平铺返回，按资源与开始时间排序")
    private List<Task> tasks = new ArrayList<>();

    @Schema(description = "资源分组元数据，按资源维度渲染时作父行")
    private List<ResourceGroup> resourceGroups = new ArrayList<>();

    @Schema(description = "工单分组元数据，按工单维度渲染时作父行")
    private List<OrderGroup> orderGroups = new ArrayList<>();

    @Schema(description = "任务依赖连线")
    private List<Link> links = new ArrayList<>();

    /**
     * 甘特图的资源分组：一台生产资源。
     *
     * <p>只带分组自身的统计，不嵌任务条：任务在 {@code tasks} 里平铺，前端按 {@code resId} 归组。
     */
    @Schema(description = "管理后台 - APS-甘特图资源分组")
    @Data
    public static class ResourceGroup {

        @Schema(description = "资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long resId;

        @Schema(description = "资源编码")
        private String resCode;

        @Schema(description = "资源名称")
        private String resName;

        @Schema(description = "资源上的任务条数", example = "12")
        private Integer taskCount;

        @Schema(description = "资源上的计划占用分钟合计", example = "1440")
        private Integer loadMinutes;

        @Schema(description = "资源上最早的计划开始时间")
        private LocalDateTime planStartTime;

        @Schema(description = "资源上最晚的计划结束时间")
        private LocalDateTime planEndTime;

    }

    /**
     * 甘特图的工单分组：一张生产工单。
     *
     * <p>交期取自 {@code aps_projection_order}，工单甘特的核心价值就是"计划完工 vs 交期"的对比，
     * 缺了交期这个视图只剩一串工序条。是否超期由后端判定，避免前端各写一套比较口径。
     */
    @Schema(description = "管理后台 - APS-甘特图工单分组")
    @Data
    public static class OrderGroup {

        @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long prodOrderId;

        @Schema(description = "生产工单号")
        private String prodOrderNo;

        @Schema(description = "生产物料编码")
        private String matCode;

        @Schema(description = "生产物料名称")
        private String matName;

        @Schema(description = "交期")
        private LocalDateTime dueDate;

        @Schema(description = "工单上的任务条数", example = "5")
        private Integer taskCount;

        @Schema(description = "工单上的计划占用分钟合计", example = "1440")
        private Integer loadMinutes;

        @Schema(description = "工单最早的计划开始时间")
        private LocalDateTime planStartTime;

        @Schema(description = "工单最晚的计划结束时间")
        private LocalDateTime planEndTime;

        @Schema(description = "是否超期：计划完工晚于交期")
        private Boolean delayFlag;

        @Schema(description = "超期分钟数，未超期为 0", example = "0")
        private Integer delayMinutes;

    }

    /** 甘特图上的一个任务条。 */
    @Schema(description = "管理后台 - APS-甘特图任务条")
    @Data
    public static class Task {

        @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "排程任务号")
        private String taskNo;

        @Schema(description = "生产工单编号，工单维度的分组键", example = "1")
        private Long prodOrderId;

        @Schema(description = "生产工单号")
        private String prodOrderNo;

        @Schema(description = "排定生产资源编号，资源维度的分组键", example = "1")
        private Long resId;

        @Schema(description = "资源编码")
        private String resCode;

        @Schema(description = "资源名称")
        private String resName;

        @Schema(description = "工序编码")
        private String opCode;

        @Schema(description = "工序名称")
        private String opName;

        @Schema(description = "项目名称")
        private String projectName;

        @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime startTime;

        @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime endTime;

        @Schema(description = "计划占用分钟数", example = "60")
        private Integer durationMinutes;

        @Schema(description = "换型损失分钟数，已含在占用分钟内", example = "0")
        private Integer changeoverMinutes;

        @Schema(description = "资源内执行顺序号", example = "1")
        private Integer sequenceNo;

        @Schema(description = "是否锁定不可移动")
        private Boolean lockedFlag;

        @Schema(description = "任务状态", example = "PLANNED")
        private String status;

    }

    /** 任务之间的依赖连线。 */
    @Schema(description = "管理后台 - APS-甘特图依赖连线")
    @Data
    public static class Link {

        @Schema(description = "前置任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long fromTaskId;

        @Schema(description = "后置任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Long toTaskId;

        @Schema(description = "依赖类型", example = "FINISH_START")
        private String depType;

        @Schema(description = "依赖间隔分钟数", example = "10")
        private Integer lagMinutes;

    }

}
