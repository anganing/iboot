package com.iboot.pf.module.aps.service.solve.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 求解进度。
 *
 * <p>同时用于 WebSocket 推送与 REST 补偿查询，所以要能序列化进 Redis。
 * 两条通路缺一不可：WebSocket 负责实时，REST 负责刷新页面或断线重连时把已经发生的进度补回来。
 */
@Data
public class ApsSolveProgress implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 排程版本编号。 */
    private Long versionId;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** 进度状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsSolveStatusEnum}。 */
    private String status;

    /** 进度百分比，0-100。 */
    private Integer ratio;

    /** 当前步骤序号。 */
    private Integer step;

    /** 总步骤数。 */
    private Integer totalStep;

    /** 当前最优得分的可读表示。 */
    private String score;

    /** 硬约束得分。 */
    private Long hardScore;

    /** 软约束得分。 */
    private Long softScore;

    /** 最近一条进度说明。 */
    private String message;

    /** 求解开始时间。 */
    private LocalDateTime startTime;

    /** 最近一次更新时间。 */
    private LocalDateTime occurTime;

    /** 进度日志，按时间正序。 */
    private List<ApsSolveProgressLog> logs = new ArrayList<>();

}
