package com.iboot.pf.module.mes.service.materialreq;

import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;

import java.util.Collection;

/**
 * 领退补料申请领域动作。
 *
 * <p>与 codegen 的 {@link MaterialReqService} 并存：codegen Service 负责手工建单改单与查询，
 * 本服务负责按用料关系展开明细，以及一切带库存后果的状态推进。
 *
 * <p>领料分三段，每段的库存后果必须分清（见 {@code dev-docs/业务流程/04-MES生产执行流程.md}
 * 的"流程四：领料三段"，以领 100 个、初始现存 500 为例）：
 *
 * <table>
 *     <tr><th>阶段</th><th>在库</th><th>预留</th><th>可用量</th></tr>
 *     <tr><td>申请后 DRAFT</td><td>500</td><td>0</td><td>500</td></tr>
 *     <tr><td>提交后 SUBMITTED</td><td>500</td><td>100</td><td>400</td></tr>
 *     <tr><td>确认后 DONE</td><td>400</td><td>0</td><td>400</td></tr>
 * </table>
 *
 * <p>确认后可用量不变（400 → 400）才是对的：料已经从仓库出到车间，仓库可用量本来就该是 400。
 * 如果确认后变成 300，说明预留没被核销，就是双重占用。
 */
public interface MesMaterialReqDomainService {

    /**
     * 创建领退补料申请，落 {@code DRAFT}，无库存后果。
     *
     * <p>明细留空时按用料关系展开：给了工序任务取该工序的路线用料，否则取整单 BOM。
     * 倒冲件不出现在明细里（报工时反冲），虚拟件被展开成子件。
     *
     * @param bo 创建参数，幂等键格式 {@code UI:{工单号}:CREATE_REQ:0}
     * @return 领退补料申请编号；幂等命中时返回首次创建的那张
     */
    Long createMaterialReq(MesCreateMaterialReqBO bo);

    /**
     * 提交领退补料申请：{@code DRAFT} -&gt; {@code SUBMITTED}。
     *
     * <p>领料与补料逐行调 WMS 预留，把料留住（可用量下降），预留编号记在行上；
     * 退料不预留——退料是往仓库还货，占用可用量没有意义。
     *
     * <p>任一行可用量不足则整单回滚，错误信息带缺料物料编码：留一半预留比全不留更难收拾，
     * 车间也无法从"部分提交"的单据判断能不能开工。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{申请单号}:SUBMIT_MATERIAL_REQ:0}
     */
    void submitMaterialReq(MesMaterialReqActionBO bo);

    /**
     * 确认领退补料申请：{@code SUBMITTED} -&gt; {@code WMS_PROCESSING} -&gt; {@code DONE} 或 {@code PARTIAL}。
     *
     * <p>这是三段里唯一改动在库量的一段。领料与补料建出库单 {@code PROD_ISSUE_OUT} 并过账，
     * 行上带提交阶段拿到的预留编号，WMS 过账后据此核销预留——现存下降、预留核销，
     * 两者抵消所以可用量不变；漏传预留编号的话预留永不核销，可用量永久偏低。
     * 退料建入库单 {@code PROD_RETURN_IN} 并过账，现存上升。
     *
     * <p>确认是一次性动作：幂等键整单固定，重复确认直接命中幂等不产生第二笔过账。
     * 本次确认量小于申请量时单据落 {@code PARTIAL}，剩余量另开补料单，不挂在原单上等。
     *
     * @param bo 确认参数，幂等键格式 {@code MES:{申请单号}:CONFIRM_MATERIAL_REQ:0}
     */
    void confirmMaterialReq(MesConfirmMaterialReqBO bo);

    /**
     * 取消领退补料申请，置 {@code CANCELLED}。
     *
     * <p>{@code DRAFT} 直接取消；{@code SUBMITTED} 先逐行释放预留再取消，
     * 不释放就等于把可用量永久占住，别的工单再也领不到这批料。
     * 已交给 WMS（{@code WMS_PROCESSING} 及之后）的单据不允许取消，料已经在路上或已出库。
     *
     * @param bo 动作参数，幂等键格式 {@code UI:{申请单号}:CANCEL_MATERIAL_REQ:0}
     */
    void cancelMaterialReq(MesMaterialReqActionBO bo);

    /**
     * 校验领退补料申请处于 {@code DRAFT}，可以被表单修改或删除。
     *
     * <p>状态规则属于领域侧，表单入口只转调这一个校验。codegen 的
     * {@link MaterialReqService#updateMaterialReq} 只拦"已交给 WMS"的单据，
     * <b>{@code SUBMITTED} 是放过去的</b>，而那正是最危险的一档：提交时已经逐行去 WMS 占了预留，
     * 此时改数量、加行、删行，预留与明细当场脱节 —— 预留按旧数量占着可用量，
     * 确认发料却按新数量走，多出来的那部分永远没人释放。要改先取消（取消会把预留还回去），再重开一张。
     *
     * @param materialReqId 领退补料申请编号
     * @throws com.iboot.pf.framework.common.exception.ServiceException 不是 {@code DRAFT} 时抛
     *         {@code MATERIAL_REQ_STATUS_INVALID}
     */
    void validateReqDraft(Long materialReqId);

    /**
     * 批量校验领退补料申请处于 {@code DRAFT}，供批量删除用。
     *
     * <p>一次查回全部申请再逐条判，不逐个 ID 查库。任一条不是 {@code DRAFT} 就整批拒绝。
     *
     * @param materialReqIds 领退补料申请编号集合，空集合直接返回
     */
    void validateReqDraft(Collection<Long> materialReqIds);

    /**
     * 按明细行编号校验其所属申请处于 {@code DRAFT}，供明细行的删除端点用。
     *
     * <p>明细行的删除端点只拿得到行编号，所属申请要由行反查。行与申请都一次批量查回，不逐行查库。
     *
     * @param lineIds 明细行编号集合，空集合直接返回
     */
    void validateLineDraft(Collection<Long> lineIds);

}
