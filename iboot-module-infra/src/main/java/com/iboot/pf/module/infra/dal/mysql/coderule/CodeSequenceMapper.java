package com.iboot.pf.module.infra.dal.mysql.coderule;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeSequenceDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeSequenceMapper extends BaseMapperX<CodeSequenceDO> {

    default CodeSequenceDO selectByRuleIdAndPeriodKey(Long ruleId, String periodKey) {
        return selectOne(CodeSequenceDO::getRuleId, ruleId,
                CodeSequenceDO::getPeriodKey, periodKey);
    }

    /**
     * 原子分配流水号。插入返回 1，更新返回 2；更新时通过 LAST_INSERT_ID 保存本次分配值。
     */
    @Insert("""
            INSERT INTO infra_code_sequence (rule_id, period_key, current_no, create_time, update_time)
            VALUES (#{ruleId}, #{periodKey}, #{serialStart}, NOW(), NOW())
            ON DUPLICATE KEY UPDATE current_no = LAST_INSERT_ID(current_no + #{serialStep}), update_time = NOW()
            """)
    int allocateSerialNo(Long ruleId, String periodKey, Long serialStart, Long serialStep);

    @Select("SELECT LAST_INSERT_ID()")
    Long selectLastAllocatedSerialNo();

    default boolean existsByRuleId(Long ruleId) {
        return selectCount(CodeSequenceDO::getRuleId, ruleId) > 0;
    }

}
