package com.iboot.pf.module.mdm.dal.dataobject.skill;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工序技能要求 DO：技能要求挂标准工序而非路线工序，避免每条路线重复维护。
 *
 * @author iboot
 */
@TableName("mdm_op_skill_req")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpSkillReqDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 标准工序编号
     */
    private Long opId;
    /**
     * 技能编号
     */
    private Long skillId;
    /**
     * 最低技能等级
     */
    private Integer minLevel;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;

}
