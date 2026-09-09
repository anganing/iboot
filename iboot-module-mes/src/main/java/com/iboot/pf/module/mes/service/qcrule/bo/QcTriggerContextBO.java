package com.iboot.pf.module.mes.service.qcrule.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 质检规则触发上下文。
 *
 * <p>由业务动作在检验点构造，承载待检业务对象的六个匹配维度与描述性快照字段。
 * 匹配维度为 null 或 {@link com.iboot.pf.module.mes.enums.MesConstants#NONE_ID}
 * 都视为"该维度不参与限定"；描述性字段仅随上下文传递，供 T2-02 生成任务时做快照，不参与匹配。
 *
 * @author iboot
 */
@Data
public class QcTriggerContextBO {

    // ========== 参与匹配的维度 ==========

    /**
     * 检验对象类型，必填，规则 {@code object_type} 必须与之相等才可能命中
     */
    private String objectType;
    /**
     * 物料编号，null 或 0 表示不限
     */
    private Long matId;
    /**
     * 物料分类编号，null 或 0 表示不限
     */
    private Long matCatId;
    /**
     * 工序编号，null 或 0 表示不限
     */
    private Long opId;
    /**
     * 工作中心编号，null 或 0 表示不限
     */
    private Long wcId;
    /**
     * 供应商编号，null 或 0 表示不限
     */
    private Long supplierId;

    // ========== 描述性快照字段，不参与匹配 ==========

    /**
     * 业务类型，如 {@code ARRIVAL_ORDER}
     */
    private String bizType;
    /**
     * 业务单号
     */
    private String bizNo;
    /**
     * 业务明细行编号
     */
    private Long bizLineId;
    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 批次号
     */
    private String lotNo;
    /**
     * 送检数量
     */
    private BigDecimal qty;
    /**
     * 计量单位编号
     */
    private Long uomId;

}
