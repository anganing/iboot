package com.iboot.pf.module.aps.dal.dataobject.scenario;

import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.time.LocalDateTime;

/** APS 排程场景。 */
@TableName("aps_scenario") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsScenarioDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private String code;
    private String name;
    private Long plantId;
    private Long workshopId;
    private Long strategyId;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime snapshotTime;
    private String status;
    private String remark;
}
