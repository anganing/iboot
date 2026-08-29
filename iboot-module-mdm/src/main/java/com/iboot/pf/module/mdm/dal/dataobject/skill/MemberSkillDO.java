package com.iboot.pf.module.mdm.dal.dataobject.skill;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 人员技能 DO：带有效期，派工时校验资质有效性（证书到期即失效）。
 *
 * @author iboot
 */
@TableName("mdm_member_skill")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSkillDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * system 用户编号
     */
    private Long userId;
    /**
     * 技能编号
     */
    private Long skillId;
    /**
     * 技能等级
     */
    private Integer level;
    /**
     * 生效日期
     */
    private LocalDate effectiveDate;
    /**
     * 失效日期
     */
    private LocalDate expiryDate;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;

}
