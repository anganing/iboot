package com.iboot.pf.module.mdm.dal.dataobject.uomconv;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 单位换算 DO
 *
 * @author iboot
 */
@TableName("mdm_uom_conv")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UomConvDO extends BaseDO {

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
     * 来源计量单位编号
     */
    private Long fromUomId;
    /**
     * 目标计量单位编号
     */
    private Long toUomId;
    /**
     * 换算比例
     */
    private BigDecimal ratio;
    /**
     * 备注
     */
    private String remark;


}