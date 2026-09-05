package com.iboot.pf.module.mes.api.optask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskRespDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskSkipReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.OpTaskService;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 工序任务 API 实现。
 *
 * <p>门面只做转换与转调：查询与排程回写转调 codegen {@link OpTaskService}，四个状态动作转调
 * {@link MesOpTaskDomainService}——工序状态只能由领域动作推进，门面自己写状态就等于把开工门禁、
 * 报工累计、完工校验、后序串联全部绕过去。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：调用方是 APS 的发布动作或车间端，
 * 不带用户上下文，按登录用户过滤会一条都取不到、一条都更不动。
 */
@Service
@Validated
@DataPermission(enable = false)
public class OpTaskApiImpl implements OpTaskApi {

    @Resource
    private OpTaskService opTaskService;

    @Resource
    private WorkOrderService workOrderService;

    @Resource
    private MesOpTaskDomainService opTaskDomainService;

    @Override
    public List<OpTaskRespDTO> getOpTaskListByOrderIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return convertWithProdOrder(opTaskService.getOpTaskListByProdOrderIds(ids));
    }

    @Override
    public List<OpTaskRespDTO> getOpTaskListByRouteOpIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return convertWithProdOrder(opTaskService.getOpTaskListByRouteOpIds(ids));
    }

    @Override
    public int applySchedule(ApplyScheduleReqDTO reqDTO) {
        // 数据权限由类上的注解关掉：调用方是 APS 的发布动作，不带用户上下文，按登录用户过滤会一条都更不到
        return opTaskService.applySchedule(reqDTO);
    }

    @Override
    public void startOpTask(OpTaskStatusReqDTO reqDTO) {
        opTaskDomainService.startOpTask(BeanUtils.toBean(reqDTO, MesOpTaskActionBO.class));
    }

    @Override
    public void pauseOpTask(OpTaskStatusReqDTO reqDTO) {
        opTaskDomainService.pauseOpTask(BeanUtils.toBean(reqDTO, MesOpTaskActionBO.class));
    }

    @Override
    public void finishOpTask(OpTaskFinishReqDTO reqDTO) {
        MesOpTaskFinishBO bo = BeanUtils.toBean(reqDTO, MesOpTaskFinishBO.class);
        // 两个数量字段名不同（DTO 是 qtyGood/qtyScrap，BO 是 qualifiedQty/scrapQty），且语义已经变了：
        // 完工不再写数量，数量只由报工推进，这两个值现在是"调用方声明的累计值"，服务端拿它与工序任务上的
        // 累计值对账，不一致直接拒绝。不搬过去的话对账就成了空的，界面数据过期照样能点完工
        bo.setQualifiedQty(reqDTO.getQtyGood());
        bo.setScrapQty(reqDTO.getQtyScrap());
        opTaskDomainService.finishOpTask(bo);
    }

    @Override
    public void skipOpTask(OpTaskSkipReqDTO reqDTO) {
        MesOpSkipBO bo = BeanUtils.toBean(reqDTO, MesOpSkipBO.class);
        // DTO 叫 reasonType、BO 叫 skipReasonType：原因分类是跳序放行的必填条件之一，
        // 丢了它 ALLOW_WITH_REASON 策略下会被判成"没填原因分类"而拒绝
        bo.setSkipReasonType(reqDTO.getReasonType());
        opTaskDomainService.skipOpTask(bo);
    }

    private List<OpTaskRespDTO> convertWithProdOrder(List<OpTaskDO> tasks) {
        List<OpTaskRespDTO> result = BeanUtils.toBean(tasks, OpTaskRespDTO.class);
        Map<Long, WorkOrderDO> workOrderMap = convertMap(
                workOrderService.getWorkOrderList(convertList(tasks, OpTaskDO::getWorkOrderId)),
                WorkOrderDO::getId);
        for (OpTaskRespDTO task : result) {
            WorkOrderDO workOrder = workOrderMap.get(task.getWorkOrderId());
            task.setProdOrderId(workOrder == null ? null : workOrder.getProdOrderId());
        }
        return result;
    }

}
