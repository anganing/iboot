package com.iboot.pf.module.mes.dal.dataobject.qcresult;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-检验结果 DO
 *
 * @author iboot
 */
@TableName("mes_qc_result")
@KeySequence("mes_qc_result_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcResultDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 质检任务编号
     */
    private Long taskId;
    /**
     * 检验项目编号
     */
    private Long itemId;
    /**
     * 样本序号
     */
    private Integer sampleNo;
    /**
     * 检验项编码快照
     */
    private String itemCode;
    /**
     * 检验项名称快照
     */
    private String itemName;
    /**
     * 实测值
     */
    private String actualValue;
    /**
     * 是否合格
     */
    private Boolean passFlag;
    /**
     * 检验员编号
     */
    private Long inspectorId;
    /**
     * 检验时间
     */
    private LocalDateTime inspectTime;
    /**
     * 本项判定合格数量
     */
    private BigDecimal qtyQualified;
    /**
     * 本项判定不合格数量
     */
    private BigDecimal qtyRejected;
    /**
     * 项目结论：PASS、FAIL
     */
    private String conclusion;
    /**
     * 备注
     */
    private String remark;


}