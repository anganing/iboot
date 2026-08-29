package com.iboot.pf.module.mdm.dal.dataobject.team;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 班组成员 DO：直接引用 system 用户，不建 MDM 人员主档。
 *
 * @author iboot
 */
@TableName("mdm_team_member")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 班组编号
     */
    private Long teamId;
    /**
     * system 用户编号
     */
    private Long userId;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
