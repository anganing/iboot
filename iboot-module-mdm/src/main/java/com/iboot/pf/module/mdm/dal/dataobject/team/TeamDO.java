package com.iboot.pf.module.mdm.dal.dataobject.team;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 班组 DO：MES 派工的主数据来源。
 *
 * @author iboot
 */
@TableName("mdm_team")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 所属车间编号
     */
    private Long workshopId;
    /**
     * 常白/常夜班次编号
     */
    private Long shiftId;
    /**
     * 班组长用户编号
     */
    private Long leaderUserId;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
