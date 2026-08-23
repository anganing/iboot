package com.iboot.pf.module.mdm.dal.dataobject.op;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 标准工序 DO
 *
 * @author iboot
 */
@TableName("mdm_op")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpDO extends BaseDO {

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
     * 标准时长（分钟）
     */
    private Integer stdDuration;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}