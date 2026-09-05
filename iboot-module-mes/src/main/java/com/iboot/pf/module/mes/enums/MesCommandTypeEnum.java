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
    SKIP_OP_TASK

}
