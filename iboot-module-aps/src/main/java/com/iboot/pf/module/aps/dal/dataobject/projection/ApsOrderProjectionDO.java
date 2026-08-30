package com.iboot.pf.module.aps.dal.dataobject.projection;

import com.baomidou.mybatisplus.annotation.*; import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
/** APS 工单投影。 */
@TableName("aps_projection_order") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsOrderProjectionDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id; private Long scenarioId; private String sourceType; private Long sourceId;
    private String sourceNo; private Long prodOrderId; private Long workOrderId; private Long opTaskId; private Long plantId;
    private Long workshopId; private Long projectId; private Long projectTaskId; private Long matId; private String matCode;
    private Long routeOpId; private Long resId; private BigDecimal qty; private BigDecimal completedQty; private LocalDateTime dueDate;
    private Integer priority; private String currentStatus; private LocalDateTime actualStartTime; private LocalDateTime actualEndTime;
    private Boolean lockedFlag; private LocalDateTime sourceUpdateTime; private LocalDateTime snapshotTime; private String remark;
}
