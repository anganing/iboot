package com.iboot.pf.module.infra.dal.dataobject.coderule;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.tenant.core.aop.TenantIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编码规则的流水状态。
 */
@TableName("infra_code_sequence")
@KeySequence("infra_code_sequence_seq")
@TenantIgnore
@Data
public class CodeSequenceDO {

    @TableId
    private Long id;
    private Long ruleId;
    private String periodKey;
    private Long currentNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
