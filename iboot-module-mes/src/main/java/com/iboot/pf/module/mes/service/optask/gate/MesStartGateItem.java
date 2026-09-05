package com.iboot.pf.module.mes.service.optask.gate;

import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;

/**
 * 开工门禁的一个校验项。
 *
 * <p>门禁做成校验链而不是写死的 if 串：每一项能独立开关、独立测试，失败时给出自己那一项的
 * 具体原因，而不是笼统的"不满足开工条件"——车间拿到"不能开工"四个字什么也做不了。
 *
 * <p>实现类是 Spring Bean，各自注入自己需要的门面，自己批量取数。校验项之间不互相依赖，
 * 顺序只由 {@link #order()} 决定。
 */
public interface MesStartGateItem {

    /** 校验项名称，进错误提示与预检结果，取值见 {@link MesStartGateNames}。 */
    String name();

    /**
     * 执行顺序，小的先跑。
     *
     * <p>顺序按"最可能拦住且最便宜"排：前序未完工是最常见的拦法且只查 MES 本地表，
     * 排在最前面能让大多数不满足的情况少走几次跨模块调用。
     */
    int order();

    /**
     * 本项对这个工序任务是否启用。
     *
     * <p>开关一律取自已有的主数据标志（{@code fai_flag}、{@code res_id} 是否分配等），
     * 不新增门禁配置表：多一张配置表就多一处要维护、要初始化、要和主数据对齐的地方。
     *
     * @param task 工序任务
     * @param ctx  门禁上下文，含路线工序等配置来源
     * @return 启用返回 true
     */
    boolean enabled(OpTaskDO task, MesStartGateContext ctx);

    /**
     * 执行校验，不满足时抛对应错误码，消息里必须带可定位的具体信息。
     *
     * <p>"可定位"的标准是车间照着消息就能去现场处理：未完工工序给工序号与工序名称，
     * 缺料给物料编码与缺口量，资源与工装给编码。给物料编号、工序任务编号这种内部主键等于没给。
     *
     * @param task 工序任务
     * @param ctx  门禁上下文
     */
    void check(OpTaskDO task, MesStartGateContext ctx);

}
