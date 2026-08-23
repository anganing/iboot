package com.iboot.pf.module.mdm.dal.dataobject.routeopmat;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工序用料 DO
 *
 * @author iboot
 */
@TableName("mdm_route_op_mat")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOpMatDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 路线工序编号
     */
    private Long routeOpId;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 数量
     */
    private BigDecimal qty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 备注
     */
    private String remark;


}