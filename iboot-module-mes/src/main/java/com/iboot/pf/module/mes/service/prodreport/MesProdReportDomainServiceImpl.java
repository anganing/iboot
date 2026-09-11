package com.iboot.pf.module.mes.service.prodreport;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesCancelReportBO;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_CANCEL_OP_TASK_DONE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_TIME_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 生产报工领域动作实现。
 *
 * <p>提交报工做四件事：落报工记录、写 {@code REPORT} 流水、把数量推进到三级、按判定收尾两级工单。
 * 数量推进本身在 {@link MesProgressPropagator}，口径集中在那里，这里只负责单据与留痕。
 *
 * <p>两个动作的幂等走两条不同的路，因为载体不同：提交是创建型动作，键落在
 * {@code mes_prod_report.idempotency_key} 上走列判重（唯一索引兜并发）；取消只改状态，
 * 那一列已被提交那次占用，因此走 {@link MesCommandService} 的命令记录。
 */
@Service
public class MesProdReportDomainServiceImpl implements MesProdReportDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 整单动作没有行号，第四段固定 0。 */
    private static final String WHOLE_REPORT_LINE_NO = "0";

    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MesProgressPropagator progressPropagator;
    @Resource
    private MesQcTaskDomainService qcTaskDomainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(MesSubmitReportBO bo) {
        // 键由调用方给：键里的 seq 是"第几次报工"，服务端按已有报工数派生会让每次重试都算出新键
        Assert.notBlank(bo.getIdempotencyKey(), "报工幂等键不能为空，它是重复提交的唯一防线");
        ProdReportDO existed = prodReportMapper.selectByIdempotencyKey(bo.getIdempotencyKey());
        if (existed != null) {
            // 幂等命中：直接返回首次那条，不产生第二条记录、也不二次推进数量
            return existed.getId();
        }
        OpTaskDO task = requireOpTask(bo.getOpTaskId());
        // 只有加工中的工序能报工：待开工的还没开始干，已暂停与待检的当下不在加工，已完工的账已经封版
        if (!MesOpTaskStatusEnum.RUNNING.name().equals(task.getStatus())) {
            throw exception(OP_TASK_STATUS_INVALID);
        }
        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        BigDecimal qtyGood = nvl(bo.getQtyGood());
        BigDecimal qtyScrap = nvl(bo.getQtyScrap());
        BigDecimal qtyRework = nvl(bo.getQtyRework());
        validateQty(task, qtyGood, qtyScrap, qtyRework);

        ProdReportDO report = buildReport(bo, task, workOrder, qtyGood, qtyScrap, qtyRework);
        prodReportMapper.insert(report);
        // 流水的 qty 按 04 文档记本次合格数；from 为空表示这条报工单是新建的
        recordReportTxn(report, task, qtyGood, null,
                MesProdReportStatusEnum.SUBMITTED.name(), bo.getIdempotencyKey(), bo.getRemark());
        progressPropagator.propagate(task, qtyGood, qtyScrap, bo.getIdempotencyKey());
        handleIpqc(task, workOrder, report, qtyGood);
        return report.getId();
    }

    /**
     * 过程检验只在规则命中时建立门禁：配置了 {@code PROCESS} 但未命中规则，业务应按规则约定直接放行。
     * 任务、报工、工序的三项状态都在报工事务内同步写入，不能留下"任务已建但工序仍可继续报工"的窗口。
     */
    private void handleIpqc(OpTaskDO task, WorkOrderDO workOrder, ProdReportDO report, BigDecimal qtyGood) {
        if (!MesQcModeEnum.PROCESS.name().equals(task.getQcMode())) {
            updateReportStatus(report, MesProdReportStatusEnum.CONFIRMED);
            return;
        }
        MesCreateQcTaskBO qcTask = new MesCreateQcTaskBO();
        qcTask.setTriggerPoint(MesQcTriggerPointEnum.IPQC.name());
        qcTask.setObjectType(MesQcObjectTypeEnum.OP_TASK.name());
        qcTask.setBizType(MesBizTypeEnum.PROD_REPORT.name());
        qcTask.setBizNo(report.getReportNo());
        qcTask.setBizLineId(MesConstants.NONE_ID);
        ProdOrderDO order = prodOrderMapper.selectById(workOrder.getProdOrderId());
        qcTask.setMatId(order == null ? MesConstants.NONE_ID : order.getMatId());
        qcTask.setOpId(task.getOpId());
        qcTask.setWcId(workOrder.getWcId());
        qcTask.setQty(qtyGood);
        qcTask.setUomId(order == null ? MesConstants.NONE_ID : order.getUomId());
        if (qcTaskDomainService.createQcTask(qcTask).isEmpty()) {
            updateReportStatus(report, MesProdReportStatusEnum.CONFIRMED);
            return;
        }
        String targetStatus = MesOpTaskStateMachine.next(task.getStatus(), MesOpTaskStateMachine.ACTION_TO_QC);
        int version = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        if (opTaskMapper.update(OpTaskDO.builder().status(targetStatus).version(version + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>().eq(OpTaskDO::getId, task.getId())
                        .eq(OpTaskDO::getVersion, version)) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setStatus(targetStatus).setVersion(version + 1);
        updateReportStatus(report, MesProdReportStatusEnum.QC_PENDING);
    }

    private void updateReportStatus(ProdReportDO report, MesProdReportStatusEnum targetStatus) {
        if (prodReportMapper.update(new ProdReportDO().setStatus(targetStatus.name()),
                new LambdaUpdateWrapper<ProdReportDO>().eq(ProdReportDO::getId, report.getId())
                        .eq(ProdReportDO::getStatus, MesProdReportStatusEnum.SUBMITTED.name())) == 0) {
            throw exception(PROD_REPORT_STATUS_INVALID);
        }
        report.setStatus(targetStatus.name());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReport(MesCancelReportBO bo) {
        ProdReportDO report = requireReport(bo.getProdReportId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CANCEL_PROD_REPORT, report.getReportNo());
        commandService.executeVoid(MesCommandTypeEnum.CANCEL_PROD_REPORT, command,
                () -> doCancel(report, bo, command.getIdempotencyKey()));
    }

    /**
     * 取消的三件事：把报工记录置 {@code CANCELLED}、写反向流水、把三级数量反向扣减。
     *
     * <p>更新带 {@code WHERE status = SUBMITTED} 条件。{@code mes_prod_report} 没有 version 列，
     * 这个条件就是它的乐观锁：并发下只有一个请求能把它取消掉，另一个更新 0 行直接报状态非法。
     */
    private void doCancel(ProdReportDO report, MesCancelReportBO bo, String idempotencyKey) {
        OpTaskDO task = requireOpTask(report.getOpTaskId());
        // 已完工的工序不许取消报工：完工按"合格 + 报废 = 计划数"校验过，抽掉一笔报工会让工序停在
        // "数量不够却已完工"，而 DONE 是终态没有回退边，圆不回来
        if (MesOpTaskStatusEnum.DONE.name().equals(task.getStatus())) {
            throw exception(PROD_REPORT_CANCEL_OP_TASK_DONE, task.getOpName());
        }
        LambdaUpdateWrapper<ProdReportDO> wrapper = new LambdaUpdateWrapper<ProdReportDO>()
                .eq(ProdReportDO::getId, report.getId())
                .eq(ProdReportDO::getStatus, MesProdReportStatusEnum.SUBMITTED.name());
        if (prodReportMapper.update(new ProdReportDO()
                .setStatus(MesProdReportStatusEnum.CANCELLED.name()), wrapper) == 0) {
            throw exception(PROD_REPORT_STATUS_INVALID);
        }
        report.setStatus(MesProdReportStatusEnum.CANCELLED.name());

        BigDecimal qtyGood = nvl(report.getQtyGood());
        BigDecimal qtyScrap = nvl(report.getQtyScrap());
        // 反向流水：事务类型里没有"取消报工"，取语义最近的 REPORT 并用负数量表达冲销，
        // 与 WMS 侧红字冲销同形。键用取消动作自己的键，与提交那条流水互不判重
        recordReportTxn(report, task, qtyGood.negate(), MesProdReportStatusEnum.SUBMITTED.name(),
                MesProdReportStatusEnum.CANCELLED.name(), idempotencyKey, bo.getReason());
        // 反向推进：负增量走的是与提交完全相同的那条推进路径，两级工单靠重算自然回到正确值
        progressPropagator.propagate(task, qtyGood.negate(), qtyScrap.negate(), idempotencyKey);
    }

    /**
     * 数量校验三条。
     *
     * <p>第三条（累计不许超计划）是任务书没写但必须有的一条：完工动作按
     * {@code MesBusinessRules.validateFinishQty} 要求"合格 + 报废 <b>等于</b> 计划数"，
     * 超报之后那道工序永远凑不出这个等式，会被永久卡在加工中。返工数不参与这个等式，
     * 它既没合格也没报废。
     */
    private void validateQty(OpTaskDO task, BigDecimal qtyGood, BigDecimal qtyScrap, BigDecimal qtyRework) {
        if (qtyGood.signum() < 0 || qtyScrap.signum() < 0 || qtyRework.signum() < 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
        if (qtyGood.add(qtyScrap).add(qtyRework).signum() == 0) {
            throw exception(PROD_REPORT_QTY_EMPTY);
        }
        BigDecimal accumulated = nvl(task.getCompletedQty()).add(nvl(task.getScrapQty()))
                .add(qtyGood).add(qtyScrap);
        if (accumulated.compareTo(nvl(task.getPlannedQty())) > 0) {
            throw exception(PROD_REPORT_QTY_EXCEEDED, task.getOpName(), qtyGood, qtyScrap,
                    accumulated, task.getPlannedQty());
        }
    }

    /**
     * 组装报工记录。
     *
     * <p>生产工单与作业工单编号从工序任务反查，调用方只给工序任务：让前端传这两个编号，
     * 它就有机会传错，报工记录会挂到别的工单上。
     *
     * <p>作业时间三个列在库表里是 NOT NULL，缺省一律取报工时间：现场扫码报工往往只按一下，
     * 逼调用方填开始与结束时间只会得到两个假数据。
     */
    private ProdReportDO buildReport(MesSubmitReportBO bo, OpTaskDO task, WorkOrderDO workOrder,
                                     BigDecimal qtyGood, BigDecimal qtyScrap, BigDecimal qtyRework) {
        LocalDateTime reportTime = bo.getReportTime() == null ? LocalDateTime.now() : bo.getReportTime();
        LocalDateTime startTime = bo.getStartTime() == null ? reportTime : bo.getStartTime();
        LocalDateTime endTime = bo.getEndTime() == null ? reportTime : bo.getEndTime();
        if (endTime.isBefore(startTime)) {
            throw exception(PROD_REPORT_TIME_INVALID);
        }
        return ProdReportDO.builder()
                .reportNo(codeService.generate(MesCodeRuleConstants.PROD_REPORT, null))
                .prodOrderId(workOrder.getProdOrderId())
                .workOrderId(workOrder.getId())
                .opTaskId(task.getId())
                .workerId(nvlId(bo.getWorkerId()))
                .resId(nvlId(bo.getResId()))
                .shiftId(nvlId(bo.getShiftId()))
                .qtyGood(qtyGood)
                .qtyScrap(qtyScrap)
                .qtyRework(qtyRework)
                .startTime(startTime)
                .endTime(endTime)
                .reportTime(reportTime)
                .status(MesProdReportStatusEnum.SUBMITTED.name())
                .idempotencyKey(bo.getIdempotencyKey())
                .remark(bo.getRemark())
                .build();
    }

    /**
     * 写一条报工流水。
     *
     * <p>不用 Recorder 的 {@code recordReport} 便捷方法：那个方法把来源业务记成工序任务，
     * 一道工序分批报三次就会出现三条同 {@code biz_no} 的流水，事后分不清是哪一次。
     * 这里记 {@code PROD_REPORT} 与报工单号，能从流水直接反查到那条报工记录；
     * {@code from/to} 记的是报工单自己的状态变化，与 {@code bizType} 指向的主体一致。
     */
    private void recordReportTxn(ProdReportDO report, OpTaskDO task, BigDecimal qty, String fromStatus,
                                 String toStatus, String idempotencyKey, String remark) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(report.getProdOrderId())
                .setWorkOrderId(report.getWorkOrderId())
                .setOpTaskId(task.getId())
                .setTxnType(MesWipTxnTypeEnum.REPORT)
                .setQty(qty)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.PROD_REPORT)
                .setBizNo(report.getReportNo())
                .setIdempotencyKey(idempotencyKey)
                .setRemark(remark));
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{报工单号}:{命令类型}:0} 派生。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType, String reportNo) {
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        ReportCommand command = new ReportCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? reportNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), reportNo,
                commandType.name(), WHOLE_REPORT_LINE_NO)
                : source.getIdempotencyKey());
        return command;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? MesConstants.ZERO_QTY : value;
    }

    /** 可选维度落 0 不落 null，与库表 NOT NULL DEFAULT 0 的口径一致。 */
    private Long nvlId(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private OpTaskDO requireOpTask(Long id) {
        OpTaskDO task = id == null ? null : opTaskMapper.selectById(id);
        if (task == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        return task;
    }

    private WorkOrderDO requireWorkOrder(Long id) {
        WorkOrderDO workOrder = id == null ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    private ProdReportDO requireReport(Long id) {
        ProdReportDO report = id == null ? null : prodReportMapper.selectById(id);
        if (report == null) {
            throw exception(PROD_REPORT_NOT_EXISTS);
        }
        return report;
    }

    /** 幂等命令载体，{@link MesCommandReqDTO} 是抽象类，报工域给它一个最小实现。 */
    private static class ReportCommand extends MesCommandReqDTO {
    }

}
