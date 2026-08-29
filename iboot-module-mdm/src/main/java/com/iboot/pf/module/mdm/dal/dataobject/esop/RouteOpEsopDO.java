package com.iboot.pf.module.mdm.dal.dataobject.esop;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工序 ESOP 作业步骤 DO：挂在路线工序下，随工艺路线版本冻结。附件见 {@link RouteOpEsopFileDO}。
 *
 * @author iboot
 */
@TableName("mdm_route_op_esop")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOpEsopDO extends BaseDO {

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
     * 展示顺序
     */
    private Integer seq;
    /**
     * 标题
     */
    private String title;
    /**
     * 作业要点文本
     */
    private String content;
    /**
     * 工艺参数 JSON
     */
    private String paramJson;
    /**
     * 是否要求作业者确认阅读
     */
    private Boolean requiredConfirmFlag;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;

}
