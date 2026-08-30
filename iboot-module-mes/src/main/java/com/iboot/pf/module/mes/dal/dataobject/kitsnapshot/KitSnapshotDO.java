package com.iboot.pf.module.mes.dal.dataobject.kitsnapshot;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-齐套检查快照 DO
 *
 * @author iboot
 */
@TableName("mes_kit_snapshot")
@KeySequence("mes_kit_snapshot_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitSnapshotDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 齐套检查编号
     */
    private String checkNo;
    /**
     * 检查范围：PROD_ORDER、WORK_ORDER、OP_TASK
     */
    private String checkScope;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 作业工单编号，0表示整单检查
     */
    private Long workOrderId;
    /**
     * 工序任务编号，0表示非工序检查
     */
    private Long opTaskId;
    /**
     * 检查明细行号
     */
    private Integer lineNo;
    /**
     * 需求物料编号
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
     * 检查时可用数量
     */
    private BigDecimal availQty;
    /**
     * 检查时本需求已预留数量
     */
    private BigDecimal reservedQty;
    /**
     * 检查时已领用数量
     */
    private BigDecimal issuedQty;
    /**
     * 短缺数量
     */
    private BigDecimal shortQty;
    /**
     * 实际采用替代料编号，0表示未采用
     */
    private Long altMatId;
    /**
     * 检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE
     */
    private String result;
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    /**
     * 备注
     */
    private String remark;


}