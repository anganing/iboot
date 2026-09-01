package com.iboot.pf.module.aps.controller.admin.version.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 求解进度 Response VO。
 *
 * <p>与 WebSocket 推送的进度结构保持一致，前端可以用同一套渲染逻辑处理两条通路的数据。
 */
@Schema(description = "管理后台 - APS-求解进度 Response VO")
@Data
public class SolveProgressRespVO {

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "进度状态", example = "SOLVING")
    private String status;

    @Schema(description = "进度百分比", example = "50")
    private Integer ratio;

    @Schema(description = "当前步骤", example = "2")
    private Integer step;

    @Schema(description = "总步骤数", example = "4")
    private Integer totalStep;

    @Schema(description = "当前最优得分", example = "0hard/-1200soft")
    private String score;

    @Schema(description = "硬约束得分", example = "0")
    private Long hardScore;

    @Schema(description = "软约束得分", example = "-1200")
    private Long softScore;

    @Schema(description = "最近一条进度说明")
    private String message;

    @Schema(description = "求解开始时间")
    private LocalDateTime startTime;

    @Schema(description = "最近一次更新时间")
    private LocalDateTime occurTime;

    @Schema(description = "进度日志")
    private List<Log> logs = new ArrayList<>();

    /** 单条进度日志。 */
    @Schema(description = "管理后台 - APS-求解进度日志")
    @Data
    public static class Log {

        @Schema(description = "进度状态", example = "SOLVING")
        private String status;

        @Schema(description = "日志内容")
        private String content;

        @Schema(description = "发生时间")
        private LocalDateTime occurTime;

    }

}
