package com.iboot.pf.module.wms.dal.dataobject.wave;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-出库波次 DO
 *
 * @author iboot
 */
@TableName("wms_wave")
@KeySequence("wms_wave_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 波次编号
     */
    private String waveNo;
    /**
     * 仓库编号
     */
    private Long whId;
    /**
     * 波次类型
     */
    private String waveType;
    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;
    /**
     * 状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}