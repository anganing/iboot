package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_RES_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 门禁四资源可用的测试。
 *
 * <p>三条判定各有失败用例：资源停用、当天无排班、并行占用达上限。产能相关的用例锁死的是
 * "按 {@code parallel_capacity} 判"这个口径——一台设备允许同时做两道工序时，
 * 已有一道在做不该算不可用。
 */
@Import(ResourceAvailableChecker.class)
class ResourceAvailableCheckerTest extends BaseDbUnitTest {

    private static final Long RES_ID = 501L;
    private static final LocalDate CHECK_DATE = LocalDate.of(2026, 9, 5);

    @Resource
    private ResourceAvailableChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private ResourceApi resourceApi;

    @Test
    void testEnabled_noResourceSkipped() {
        // 手工工位没有设备可占
        OpTaskDO task = insertTask(MesConstants.NONE_ID, MesOpTaskStatusEnum.READY);

        assertFalse(checker.enabled(task, ctx()));
    }

    @Test
    void testCheck_disabledResourceRejected() {
        // 准备：资源已停用。getResourceMap 不过滤停用，要自己判
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        givenResource(1, 1);
        givenCalendar(true);

        assertServiceException(() -> checker.check(task, ctx()),
                OP_TASK_RES_UNAVAILABLE, "RES-501 加工中心", "资源已停用");
    }

    @Test
    void testCheck_noCalendarRejected() {
        // 准备：资源启用但当天没有排班
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        givenResource(0, 1);
        givenCalendar(false);

        // 调用并断言：消息带日期，车间知道是哪天没排班
        assertServiceException(() -> checker.check(task, ctx()),
                OP_TASK_RES_UNAVAILABLE, "RES-501 加工中心", CHECK_DATE + " 没有排班");
    }

    @Test
    void testCheck_parallelCapacityReachedRejected() {
        // 准备：产能 1，同一资源上已有一道工序在加工
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        insertTask(RES_ID, MesOpTaskStatusEnum.RUNNING);
        givenResource(0, 1);
        givenCalendar(true);

        assertServiceException(() -> checker.check(task, ctx()),
                OP_TASK_RES_UNAVAILABLE, "RES-501 加工中心", "已有 1 道工序在加工，并行产能上限 1");
    }

    @Test
    void testCheck_parallelCapacityNotReachedPassed() {
        // 准备：产能 2，已有一道在加工
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        insertTask(RES_ID, MesOpTaskStatusEnum.RUNNING);
        givenResource(0, 2);
        givenCalendar(true);

        // 调用：一台设备允许同时做两道工序时，已有一道在做不该算不可用
        checker.check(task, ctx());
    }

    @Test
    void testCheck_idleResourcePassed() {
        // 准备：资源启用、有排班、没有别的工序在做
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        givenResource(0, 1);
        givenCalendar(true);

        checker.check(task, ctx());
    }

    @Test
    void testCheck_selfRunningNotCountedAsOccupied() {
        // 准备：本工序自己已经是加工中（重复开工的场景）
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.RUNNING);
        givenResource(0, 1);
        givenCalendar(true);

        // 调用：重复开工由状态机拦，不该在这里表现成"产能不足"，那会让车间去找不存在的占用者
        checker.check(task, ctx());
    }

    @Test
    void testCheck_pausedTaskNotCountedAsOccupied() {
        // 准备：同一资源上有一道工序处于暂停
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        insertTask(RES_ID, MesOpTaskStatusEnum.PAUSED);
        givenResource(0, 1);
        givenCalendar(true);

        // 调用：暂停的工序占着工件但设备是空的，算占用会挡住"暂停一道去插一道急件"这种常规操作
        checker.check(task, ctx());
    }

    @Test
    void testCheck_nullCapacityTreatedAsOne() {
        // 准备：产能没配，同一资源上已有一道在加工
        OpTaskDO task = insertTask(RES_ID, MesOpTaskStatusEnum.READY);
        insertTask(RES_ID, MesOpTaskStatusEnum.RUNNING);
        givenResource(0, null);
        givenCalendar(true);

        // 调用并断言：产能没配按 1 处理。按无上限处理会让排产失控，一台设备被排上十道工序
        assertServiceException(() -> checker.check(task, ctx()),
                OP_TASK_RES_UNAVAILABLE, "RES-501 加工中心", "已有 1 道工序在加工，并行产能上限 1");
    }

    private MesStartGateContext ctx() {
        MesStartGateContext ctx = new MesStartGateContext();
        ctx.setCheckDate(CHECK_DATE);
        return ctx;
    }

    private void givenResource(Integer status, Integer parallelCapacity) {
        ResourceRespDTO resource = new ResourceRespDTO();
        resource.setId(RES_ID);
        resource.setCode("RES-" + RES_ID);
        resource.setName("加工中心");
        resource.setStatus(status);
        resource.setParallelCapacity(parallelCapacity);
        when(resourceApi.getResourceMap(any())).thenReturn(Map.of(RES_ID, resource));
    }

    private void givenCalendar(boolean scheduled) {
        if (!scheduled) {
            when(resourceApi.getResourceCalendarList(any(), any(), any())).thenReturn(List.of());
            return;
        }
        ResourceCalendarRespDTO calendar = new ResourceCalendarRespDTO();
        calendar.setId(1L);
        calendar.setResId(RES_ID);
        calendar.setWorkDate(CHECK_DATE);
        calendar.setAvailableMinutes(480);
        when(resourceApi.getResourceCalendarList(any(), any(), any())).thenReturn(List.of(calendar));
    }

    private OpTaskDO insertTask(Long resId, MesOpTaskStatusEnum status) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + System.nanoTime())
                .workOrderId(9001L)
                .workOrderNo("WO-0001")
                .routeOpId(101L)
                .opId(201L)
                .opCode("OP010")
                .opName("下料")
                .seq(10)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(new BigDecimal("100"))
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(resId)
                .keyFlag(Boolean.FALSE)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

}

