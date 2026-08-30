package com.iboot.pf.module.wms.dal.dataobject.waveline;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-波次明细 DO
 *
 * @author iboot
 */
@TableName("wms_wave_line")
@KeySequence("wms_wave_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaveLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 波次编号
     */
    private Long waveId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 出库单编号
     */
    private Long outOrderId;
    /**
     * 出库单明细编号
     */
    private Long outOrderLineId;
    /**
     * 波次内优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 明细状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}