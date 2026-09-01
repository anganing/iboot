package com.iboot.pf.module.aps.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 排程甘特图 Response VO。
 *
 * <p>一次返回甘特图渲染需要的全部数据：资源分组、资源内的任务条、任务之间的依赖连线、时间窗。
 * 不复用 {@code /aps/task/page}：甘特图要的是整版本的完整视图，分页拉回来的片段既画不出连线，
 * 也算不出时间轴范围。
 *
 * <p>按资源分组而不是平铺：制造业看甘特的第一诉求是"每台设备什么时候在干什么"，
 * 资源是行、任务是行内的条，这个结构直接对应 vtable-gantt 的父子任务模型。
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

    @Schema(description = "资源分组")
    private List<Resource> resources = new ArrayList<>();

    @Schema(description = "任务依赖连线")
    private List<Link> links = new ArrayList<>();

    /** 甘特图的一行：一台生产资源及其上排定的任务。 */
    @Schema(description = "管理后台 - APS-甘特图资源行")
    @Data
    public static class Resource {

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

        @Schema(description = "资源上的任务条，按开始时间升序")
        private List<Task> tasks = new ArrayList<>();

    }

    /** 甘特图上的一个任务条。 */
    @Schema(description = "管理后台 - APS-甘特图任务条")
    @Data
    public static class Task {

        @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "排程任务号")
        private String taskNo;

        @Schema(description = "生产工单号")
        private String prodOrderNo;

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
