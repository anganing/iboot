package com.iboot.pf.module.aps.dal.dataobject.command;

import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * APS 命令幂等记录。
 */
@TableName("aps_command_record")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsCommandRecordDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String idempotencyKey;
    private String commandType;
    private String sourceModule;
    private String sourceBizNo;
    private String sourceBizLineNo;
    private Long resultId;
}
