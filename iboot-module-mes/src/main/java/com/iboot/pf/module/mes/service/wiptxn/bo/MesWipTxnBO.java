package com.iboot.pf.module.mes.service.wiptxn.bo;

import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在制流水写入参数。
 *
 * <p>字段与 {@code mes_wip_txn} 的业务列一一对应：{@code id} 由主键策略分配、{@code txn_no} 由
 * {@link com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder} 走编码规则生成、审计列由框架填充，
 * 因此这三类不在本参数里。
 *
 * <p>下达、开工、报工、完工、跳序、领料这些动作一律经 Recorder 写入，不各自手写 DO，
 * 否则字段口径会漂移、幂等键会有人漏。
 *
 * <p>{@code txnType} 与 {@code bizType} 直接用枚举类型而非字符串，让"禁止字符串字面量"在编译期就成立。
 */
@Data
public class MesWipTxnBO {

    /** 生产工单编号，必填。这是追溯链的主维度，任何一条流水都必须挂在生产工单上。 */
    private Long prodOrderId;

    /** 作业工单编号，可选维度，为空落 0。工单级流水（如下达）没有作业工单。 */
    private Long workOrderId;

    /** 工序任务编号，可选维度，为空落 0。工单级流水没有工序任务。 */
    private Long opTaskId;

    /** 事务类型，必填。各取值的写入时机见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"在制流水"章节。 */
    private MesWipTxnTypeEnum txnType;

    /** 事务数量，为空落 0。状态类事务（开工、暂停、恢复、跳序）记 0，数量类事务（报工、完工、返工、报废）记实际数量。 */
    private BigDecimal qty;

    /** 变更前状态，可为空（无前状态的场景，如首次留痕）。取值一律引对应状态枚举的 {@code name()}。 */
    private String fromStatus;

    /** 变更后状态，必填。与 {@code fromStatus} 共同构成状态机执行轨迹的唯一凭据。 */
    private String toStatus;

    /** 来源业务类型，必填。配合 {@code bizNo} 定位到具体单据。 */
    private MesBizTypeEnum bizType;

    /** 来源业务单号，必填。 */
    private String bizNo;

    /** 来源工序任务编号，可选维度，为空落 0。工序流转（MOVE）与跳序（SKIP）时记流出工序。 */
    private Long fromOpTaskId;

    /** 目标工序任务编号，可选维度，为空落 0。工序流转（MOVE）与跳序（SKIP）时记流入工序。 */
    private Long toOpTaskId;

    /** 事务发生时间，为空取当前时间。 */
    private LocalDateTime txnTime;

    /**
     * 事务幂等键，必填。
     *
     * <p>同一动作重复提交只留一条流水：Recorder 先按它判重，表上 {@code uk_mes_wip_txn_idem_deleted}
     * 唯一索引兜住并发。键格式见 {@code dev-docs/业务流程/08-跨模块协作与端到端时序.md} 的幂等键全表。
     */
    private String idempotencyKey;

    /** 备注，可为空。暂停原因、跳序原因这类可读说明记在这里。 */
    private String remark;

}
