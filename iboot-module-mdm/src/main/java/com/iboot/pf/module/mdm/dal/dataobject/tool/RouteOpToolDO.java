package com.iboot.pf.module.mdm.dal.dataobject.tool;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工序工装 DO：开工门禁校验项与 APS 辅助资源约束的数据源。
 *
 * @author iboot
 */
@TableName("mdm_route_op_tool")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOpToolDO extends BaseDO {

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
     * 工装编号
     */
    private Long toolId;
    /**
     * 需求数量
     */
    private Integer qty;
    /**
     * 是否必备
     */
    private Boolean requiredFlag;

}
