package com.iboot.pf.module.aps.service.task.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 甘特图工单分组的聚合结果。
 *
 * <p>比 {@code TaskGanttRespVO.OrderGroup} 多一个 {@code matId}：物料名称落在 mdm，要靠它调门面批量补全，
 * 补完就不再往前端传，前端只拿编码与名称。是否超期也不在这里判，留给 Service 统一口径。
 */
@Data
public class ApsGanttOrderGroupBO {

    /** 生产工单编号。 */
    private Long prodOrderId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 生产物料编号，仅用于补全物料名称。 */
    private Long matId;

    /** 生产物料编码。 */
    private String matCode;

    /** 生产物料名称，由 mdm 门面批量补全。 */
    private String matName;

    /** 交期。 */
    private LocalDateTime dueDate;

    /** 工单上的任务条数。 */
    private Integer taskCount;

    /** 工单上的计划占用分钟合计。 */
    private Integer loadMinutes;

    /** 工单最早的计划开始时间。 */
    private LocalDateTime planStartTime;

    /** 工单最晚的计划结束时间。 */
    private LocalDateTime planEndTime;

}
