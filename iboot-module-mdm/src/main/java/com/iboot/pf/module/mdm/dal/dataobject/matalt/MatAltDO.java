package com.iboot.pf.module.mdm.dal.dataobject.matalt;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 替代料 DO
 *
 * @author iboot
 */
@TableName("mdm_mat_alt")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatAltDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 替代物料编号
     */
    private Long altMatId;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 备注
     */
    private String remark;


}