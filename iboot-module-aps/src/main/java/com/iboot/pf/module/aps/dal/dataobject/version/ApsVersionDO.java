package com.iboot.pf.module.aps.dal.dataobject.version;

import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.time.LocalDateTime;

/** APS 排程版本。 */
@TableName("aps_version") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsVersionDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long scenarioId;
    private Integer versionNo;
    private String status;
    private String score;
    private Long hardScore;
    private Long softScore;
    private LocalDateTime solveStartTime;
    private LocalDateTime solveEndTime;
    private LocalDateTime publishedTime;
    private Long publishedBy;
    private String solverMessage;
    @Version private Integer version;
    private String remark;
}
