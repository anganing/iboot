package com.iboot.pf.module.wms.dal.dataobject.countorder;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-盘点单 DO
 *
 * @author iboot
 */
@TableName("wms_count_order")
@KeySequence("wms_count_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 盘点单号
     */
    private String countNo;
    /**
     * 盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC
     */
    private String countType;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 仓库编号
     */
    private Long whId;
    /**
     * 库区编号，0表示全部库区
     */
    private Long zoneId;
    /**
     * 项目编号，0表示不限项目
     */
    private Long projectId;
    /**
     * 账面快照时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 是否盲盘
     */
    private Boolean blindCountFlag;
    /**
     * 状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED
     */
    private String status;
    /**
     * 创建幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}