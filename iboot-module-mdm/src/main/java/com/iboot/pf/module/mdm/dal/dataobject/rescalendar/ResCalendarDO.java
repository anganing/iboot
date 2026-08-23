package com.iboot.pf.module.mdm.dal.dataobject.rescalendar;

import java.time.LocalDate;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 资源日历 DO
 *
 * @author iboot
 */
@TableName("mdm_res_calendar")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResCalendarDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 资源编号
     */
    private Long resId;
    /**
     * 工作日期
     */
    private LocalDate workDate;
    /**
     * 班次编号
     */
    private Long shiftId;
    /**
     * 可用产能分钟
     */
    private Integer availableMinutes;
    /**
     * 备注
     */
    private String remark;


}