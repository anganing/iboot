package com.iboot.pf.module.mdm.dal.dataobject.rescapability;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 资源能力 DO：记录工序可在哪些资源上加工，是 APS 资源投影能力数据的唯一来源。
 *
 * @author iboot
 */
@TableName("mdm_res_capability")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResCapabilityDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 生产资源编号
     */
    private Long resId;
    /**
     * 标准工序编号
     */
    private Long opId;
    /**
     * 是否首选资源
     */
    private Boolean preferredFlag;
    /**
     * 该资源做该工序的专属效率系数
     */
    private BigDecimal efficiency;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;

}
