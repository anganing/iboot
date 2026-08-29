package com.iboot.pf.module.mdm.dal.dataobject.res;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 生产资源 DO
 *
 * @author iboot
 */
@TableName("mdm_res")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 工作中心编号
     */
    private Long wcId;
    /**
     * 资源类型
     */
    private String resType;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 效率系数
     */
    private BigDecimal efficiency;
    /**
     * 可同时承载任务数
     */
    private Integer parallelCapacity;
    /**
     * 资源组，同组可互替排程
     */
    private String resGroup;
    /**
     * 默认日历模板编号
     */
    private Long calendarId;
    /**
     * 工厂编号（冗余）
     */
    private Long plantId;
    /**
     * 车间编号（冗余）
     */
    private Long workshopId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}