package com.iboot.pf.module.mes.enums;

/**
 * MES 跨模块命令类型。
 *
 * <p>写进 {@code mes_command_record.command_type}，配合幂等键做重复下发拦截。
 * 集中定义而不是在各 Service 里写字面量：改一个名字就会和历史记录对不上。
 *
 * <p>只登记没有落库载体的状态推进型命令。创建型命令（建需求、建领料申请、报工、在制流水、
 * 生成质检任务）业务表自带 {@code idempotency_key} 唯一索引，走列判重，不进这里。
 */
public enum MesCommandTypeEnum {

    /** 确认生产需求。 */
    CONFIRM_DEMAND,
    /** 取消生产需求。 */
    CANCEL_DEMAND,
    /** 生产需求转生产工单。 */
    DEMAND_TO_ORDER,
    /** 下达生产工单。 */
    RELEASE_PROD_ORDER,
    /** 挂起生产工单。 */
    HOLD_PROD_ORDER,
    /** 恢复生产工单。 */
    RESUME_PROD_ORDER,
    /** 关闭生产工单。 */
    CLOSE_PROD_ORDER,
    /** 取消生产工单。 */
    CANCEL_PROD_ORDER,
    /** 齐套检查。 */
    KIT_CHECK,
    /** 提交领料申请。 */
    SUBMIT_MATERIAL_REQ,
    /** 确认领料并发料。 */
    CONFIRM_MATERIAL_REQ,
    /** 取消领料申请。 */
    CANCEL_MATERIAL_REQ,
    /** 工序开工。 */
    START_OP_TASK,
    /** 工序暂停。 */
    PAUSE_OP_TASK,
    /** 工序恢复。 */
    RESUME_OP_TASK,
    /** 工序完工。 */
    FINISH_OP_TASK,
    /** 工序跳序。 */
    SKIP_OP_TASK,
    /** 撤销工序跳序。 */
    REVOKE_OP_SKIP,
    /**
     * 取消报工。
     *
     * <p>提交报工不在这里：{@code mes_prod_report} 自带 {@code idempotency_key} 唯一索引，走列判重。
     * 取消改的是那条记录的状态，没有新载体可落键，因此进命令记录。
     */
    CANCEL_PROD_REPORT,
    /**
     * 完工入库。
     *
     * <p>MES 侧没有承载入库的业务表，因此走命令记录：{@code result_id} 落 WMS 入库单编号，
     * 重复下发时直接返回首次那张单，不会产生第二笔过账。
     */
    FINISH_INBOUND,
    /**
     * 发布检验方案。
     *
     * <p>方案发布后进入版本锁定，规则引用与任务生成都要以它为判定口径，重复发布有命令记录拦着。
     */
    PUBLISH_QC_PLAN,
    /** 停用检验方案，启用中的规则引用着时领域校验会先拦下。 */
    DISABLE_QC_PLAN,
    // ========== 质量子域（T2-04 统一追加，后续处置与返工任务复用，只允许追加） ==========
    /**
     * 生成质检任务。
     *
     * <p>仅登记类型：{@code createQcTask} 的幂等走 {@code mes_qc_task.idempotency_key}
     * 唯一索引列判重，不写命令记录。登记它是为了让质量子域八个命令在类型表里齐备，
     * 命令记录里若出现该类型能对上口径，避免跨模块调用方自造字面量。
     */
    CREATE_QC_TASK,
    /** 指派质检任务。 */
    ASSIGN_QC_TASK,
    /** 开始检验。 */
    START_INSPECT,
    /** 取消质检任务（仅未开检的任务允许）。 */
    CANCEL_QC_TASK,
    /**
     * 提交检验结论。
     *
     * <p>结果录入（batchSaveResult）不在这里：{@code mes_qc_result} 每行落 {@code inspector_id}
     * 与 {@code inspect_time}，结果数据本身就是载体，T2-05 实现时按行判重。
     */
    SUBMIT_CONCLUSION,
    /** 提交处置单（T2-12）。 */
    SUBMIT_DISPOSITION,
    /** 执行处置单（T2-12）。 */
    EXECUTE_DISPOSITION,
    /** 下达返工任务（T2-13）。 */
    RELEASE_REWORK,
    /** 返工完工（T2-13）。 */
    FINISH_REWORK

}
