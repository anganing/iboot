package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO;

import java.math.BigDecimal;

/**
 * 完工入库。把生产工单产出的成品注册成批次、建 {@code PROD_FINISH_IN} 入库单并过账，成品库余额随之上升。
 *
 * <p>四步一步不能少（见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程七"）：
 * 校验工单在产或已完工 -&gt; {@code BatchApi.registerBatch} 注册成品批次 -&gt;
 * {@code InOrderApi.createInOrder} 建单 -&gt; {@code InOrderApi.confirmInOrder} 过账。
 * 少了注册批次，成品进库房时没有批次台账，追溯链在成品这一段断掉；少了过账，单据建了余额没动。
 *
 * <p><b>已入库量的唯一账本是 {@code mes_wip_txn}</b>：每次入库过账后写一条
 * {@code biz_type = IN_ORDER}、{@code biz_no = WMS 入库单编号}、{@code qty = 本次入库量} 的流水，
 * 累计入库量就是这些流水的数量之和。MES 侧没有承载完工入库的业务表，而 WMS 的出入库门面只有
 * {@code create} / {@code cancel} / {@code confirm} 三个写方法、一个只读查询都没有，
 * 因此"这张工单已经入了多少"只能由 MES 自己记账。
 */
public interface MesFinishInboundService {

    /**
     * 完工入库，幂等。
     *
     * <p>仓库与库位解析不出来时直接报错：手工入库是人发起的动作，把配置缺失暴露给发起人才能被修好。
     *
     * @param bo 入库参数，幂等键格式 {@code MES:{工单号}:FINISH_INBOUND:{第几次}}
     * @return WMS 入库单编号；重复下发时返回首次那张单的编号
     */
    Long createFinishInbound(MesFinishInboundBO bo);

    /**
     * 生产工单转入 {@code DONE} 时自动入库，与手工入库共用同一套幂等键与账本。
     *
     * <p>与手工入库的唯一区别是<b>配置缺失时静默跳过而不报错</b>：完工是车间现场动作，
     * 不该因为成品物料没配默认仓库而完不了工。没入的量仍留在"已完工减已入库"的差额里，
     * 事后手工调 {@link #createFinishInbound} 补入即可。
     *
     * <p>剩余可入库量为零时也直接返回：分批入库已经把该入的入完了。
     *
     * @param prodOrderId 生产工单编号
     * @param workOrderId 带动完工的那道工序所属作业工单编号，只作追溯快照
     * @return WMS 入库单编号；跳过时返回 {@code null}
     */
    Long autoInboundOnOrderDone(Long prodOrderId, Long workOrderId);

    /**
     * 一张生产工单的累计入库量。
     *
     * @param prodOrderId 生产工单编号
     * @return 累计入库量，从未入库时为零
     */
    BigDecimal inboundedQty(Long prodOrderId);

}
