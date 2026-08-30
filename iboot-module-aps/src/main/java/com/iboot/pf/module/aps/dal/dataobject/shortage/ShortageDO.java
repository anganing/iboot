package com.iboot.pf.module.aps.dal.dataobject.shortage;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 缺料清单 DO
 *
 * @author iboot
 */
@TableName("aps_shortage")
@KeySequence("aps_shortage_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortageDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程版本编号
     */
    private Long versionId;
    /**
     * 生产工单编号，0表示汇总缺料
     */
    private Long prodOrderId;
    /**
     * 工序任务编号，0表示非工序级缺料
     */
    private Long opTaskId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 短缺物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 需求数量
     */
    private BigDecimal reqQty;
    /**
     * 可用数量
     */
    private BigDecimal availQty;
    /**
     * 短缺数量
     */
    private BigDecimal shortQty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 需求到位时间
     */
    private LocalDateTime needDate;
    /**
     * 预计供给时间
     */
    private LocalDateTime expectedSupplyDate;
    /**
     * 状态：OPEN、PARTIAL、RESOLVED、IGNORED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}