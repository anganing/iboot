package com.iboot.pf.module.mes.dal.dataobject.qcplan;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-检验方案 DO
 *
 * @author iboot
 */
@TableName("mes_qc_plan")
@KeySequence("mes_qc_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcPlanDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 检验方案编码
     */
    private String code;
    /**
     * 检验方案名称
     */
    private String name;
    /**
     * 方案版本号
     */
    private String versionNo;
    /**
     * 抽样类型：FULL、FIXED_QTY、PERCENT、AQL
     */
    private String sampleType;
    /**
     * 固定数量或抽样比例
     */
    private BigDecimal sampleValue;
    /**
     * AQL接收质量限
     */
    private String aql;
    /**
     * 接收判定规则
     */
    private String acceptanceRule;
    /**
     * 状态：DRAFT、PUBLISHED、DISABLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}