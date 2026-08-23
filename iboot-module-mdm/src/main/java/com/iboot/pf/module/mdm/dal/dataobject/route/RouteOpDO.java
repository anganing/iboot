package com.iboot.pf.module.mdm.dal.dataobject.routeop;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工艺路线工序 DO
 *
 * @author iboot
 */
@TableName("mdm_route_op")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOpDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 工艺路线编号
     */
    private Long routeId;
    /**
     * 工序顺序
     */
    private Integer seq;
    /**
     * 标准工序编号
     */
    private Long opId;
    /**
     * 工作中心编号
     */
    private Long wcId;
    /**
     * 并行组
     */
    private String parallelGroup;
    /**
     * 是否关键工序
     */
    private Boolean keyFlag;
    /**
     * 是否首件检验
     */
    private Boolean faiFlag;
    /**
     * 备注
     */
    private String remark;

}