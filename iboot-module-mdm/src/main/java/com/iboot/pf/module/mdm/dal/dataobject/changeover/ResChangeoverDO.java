package com.iboot.pf.module.mdm.dal.dataobject.changeover;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 资源换型矩阵 DO：APS 顺序相关准备时间（sequence-dependent setup）的唯一数据源。
 *
 * @author iboot
 */
@TableName("mdm_res_changeover")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResChangeoverDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 资源编号，0-按资源组生效
     */
    private Long resId;
    /**
     * 资源组
     */
    private String resGroup;
    /**
     * 换出换型分组
     */
    private String fromGroup;
    /**
     * 换入换型分组
     */
    private String toGroup;
    /**
     * 换型分钟数
     */
    private Integer changeoverMinutes;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
