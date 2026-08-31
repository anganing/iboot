package com.iboot.pf.module.mes.dal.dataobject.qcresult;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES-检验结果 DO
 *
 * @author iboot
 */
@TableName("mes_qc_result")
@KeySequence("mes_qc_result_seq")
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
     * 样本序号，与任务、检验项组成唯一索引
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
     * 是否合格，与项目结论必须一致
     */
    private Boolean passFlag;
    /**
     * 检验员编号，来自 system_users
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
     * 项目结论
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum}
     */
    private String conclusion;
    /**
     * 备注
     */
    private String remark;

}
