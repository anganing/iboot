package com.iboot.pf.module.mdm.dal.dataobject.skill;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 技能 DO
 *
 * @author iboot
 */
@TableName("mdm_skill")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 技能编码
     */
    private String code;
    /**
     * 技能名称
     */
    private String name;
    /**
     * 等级体系说明
     */
    private String levelDesc;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
