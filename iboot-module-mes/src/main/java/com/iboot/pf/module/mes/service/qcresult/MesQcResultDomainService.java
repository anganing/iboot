package com.iboot.pf.module.mes.service.qcresult;

import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;

/**
 * 检验结果领域动作。
 *
 * <p>与 codegen 的 {@link com.iboot.pf.module.mes.service.qcresult.QcResultService} 并存，分工是硬的：
 * codegen Service 负责结果数据的增删改查，本服务负责"实测值怎么变成判定"——批量录入 + 单项判定 +
 * 录完自动推进任务，是检验录入作业页（T2-16）与质量门面（T2-14）的唯一入口。
 *
 * <p>判定口径集中在 {@link MesQcItemJudge}：一条实测值对照一个检验项给出 PASS / FAIL，任务级
 * 结论（QUALIFIED / REJECTED / CONCESSION）是人的决策，留给
 * {@code MesQcTaskDomainService.submitConclusion}。
 *
 * @author iboot
 */
public interface MesQcResultDomainService {

    /**
     * 批量保存实测值：一次提交多个检验项多个样本，逐格判定并落库。
     *
     * <p>流程：校验任务 {@code status = INSPECTING} -&gt; 逐行调 {@link MesQcItemJudge#judge} 判定
     * -&gt; 覆盖写 {@code actual_value / pass_flag / conclusion / inspector_id / inspect_time}
     * -&gt; 全部必检项都已录入时任务自动推进 {@code INSPECTING -> RESULT_POSTING}。
     *
     * <p>录入语义是"重复保存覆盖同格"：同一格改数值重交是常规操作（录错改数、补录漏项），
     * 不走命令幂等——命令幂等会把第二次提交当重复下发拦掉，改数就改不动了。任务推进带乐观锁，
     * 并发提交撞车抛 {@code QC_TASK_CONCURRENT_CONFLICT}。
     *
     * @param bo 批量录入参数，含任务编号与格列表
     * @return 本次保存的格子数
     */
    int batchSaveResult(MesSaveQcResultBO bo);

    /**
     * 按已录结果给出建议结论：全部 PASS -&gt; QUALIFIED，有 FAIL -&gt; REJECTED。
     *
     * <p>这只是建议，最终结论由人在 {@code submitConclusion} 时定（可选 CONCESSION 让步接收）。
     * 前端提交结论弹窗用它做默认值，少打一次字。
     *
     * @param taskId 质检任务编号
     * @return 建议结论，取值见 MesQcConclusionEnum
     */
    String suggestConclusion(Long taskId);

}
