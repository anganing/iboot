package com.iboot.pf.module.mes.service.optask.gate.impl;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateItem;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateNames;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_RES_UNAVAILABLE;

/**
 * 门禁四：资源可用。
 *
 * <p>三条判定，任一不满足就拦：资源本身启用、当天有排班、并行占用未达产能上限。
 *
 * <p>"未被占用"按资源的 {@code parallel_capacity} 判：一台设备允许同时做 N 道工序时，
 * 只有正在做的数量达到 N 才算不可用。产能没配或配成 0 时按 1 处理——
 * 一台设备默认同时只能做一道工序，按无上限处理会让排产失控。
 *
 * <p>资源启用状态要自己判：{@code getResourceMap} 不过滤停用资源（它是通用批量取数，
 * 过滤了别的调用方就查不到停用资源的名称）。停用的设备排了工也开不了。
 */
@Component
public class ResourceAvailableChecker implements MesStartGateItem {

    /** 资源启用状态值，与 {@code mdm_res.status} 的 0-启用 / 1-停用 一致。 */
    private static final Integer STATUS_ENABLED = 0;

    /** 并行产能缺省值：一台设备同时做一道工序。 */
    private static final int DEFAULT_PARALLEL_CAPACITY = 1;

    @Resource
    private ResourceApi resourceApi;
    @Resource
    private OpTaskMapper opTaskMapper;

    @Override
    public String name() {
        return MesStartGateNames.RESOURCE_AVAILABLE;
    }

    @Override
    public int order() {
        return 40;
    }

    /** 未分配资源（{@code res_id = 0}）的工序不校验资源：手工工位本来就没有设备可占。 */
    @Override
    public boolean enabled(OpTaskDO task, MesStartGateContext ctx) {
        return task.getResId() != null && !MesConstants.NONE_ID.equals(task.getResId());
    }

    @Override
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        Map<Long, ResourceRespDTO> resourceMap = resourceApi.getResourceMap(List.of(task.getResId()));
        ResourceRespDTO resource = resourceMap == null ? null : resourceMap.get(task.getResId());
        if (resource == null) {
            // 资源被删或数据不全时不拦：门禁拦确定不满足的情况，不拦查不到数据的情况
            return;
        }
        String resourceText = resource.getCode() + " " + resource.getName();
        if (!STATUS_ENABLED.equals(resource.getStatus())) {
            throw exception(OP_TASK_RES_UNAVAILABLE, resourceText, "资源已停用");
        }
        List<ResourceCalendarRespDTO> calendars = resourceApi.getResourceCalendarList(
                List.of(task.getResId()), ctx.getCheckDate(), ctx.getCheckDate());
        if (CollUtil.isEmpty(calendars)) {
            throw exception(OP_TASK_RES_UNAVAILABLE, resourceText, ctx.getCheckDate() + " 没有排班");
        }
        int capacity = capacityOf(resource);
        long running = runningCount(task);
        if (running >= capacity) {
            throw exception(OP_TASK_RES_UNAVAILABLE, resourceText,
                    "已有 " + running + " 道工序在加工，并行产能上限 " + capacity);
        }
    }

    private int capacityOf(ResourceRespDTO resource) {
        Integer capacity = resource.getParallelCapacity();
        return capacity == null || capacity <= 0 ? DEFAULT_PARALLEL_CAPACITY : capacity;
    }

    /**
     * 同一资源上正在加工的工序任务数。
     *
     * <p>只数 {@code RUNNING}：{@code PAUSED} 的工序虽然占着设备上的工件，但设备本身是空的，
     * 把暂停也算占用会让"暂停一道去插一道急件"这种常规操作被门禁挡住。
     * 本工序自己排除掉——重复开工由状态机拦，不该在这里表现成产能不足。
     */
    private long runningCount(OpTaskDO task) {
        List<OpTaskDO> runningTasks = opTaskMapper.selectList(
                OpTaskDO::getResId, task.getResId(),
                OpTaskDO::getStatus, MesOpTaskStatusEnum.RUNNING.name());
        return runningTasks.stream().filter(item -> !item.getId().equals(task.getId())).count();
    }

}
