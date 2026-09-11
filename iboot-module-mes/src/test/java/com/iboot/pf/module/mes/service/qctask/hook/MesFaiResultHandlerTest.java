package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 首件结论只在拒收时挂起工序，合格由后续开工门禁读取结论自行放行。 */
@Import(MesFaiResultHandler.class)
class MesFaiResultHandlerTest extends BaseDbUnitTest {

    @Resource
    private MesFaiResultHandler handler;
    @Resource
    private OpTaskMapper opTaskMapper;

    @Test
    void testOnConclusion_rejectedHoldsOpTask() {
        OpTaskDO opTask = insertRunningTask();

        handler.onConclusion(qcTask(opTask, MesQcConclusionEnum.REJECTED));

        OpTaskDO held = opTaskMapper.selectById(opTask.getId());
        assertEquals(MesOpTaskStatusEnum.HOLD.name(), held.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, held.getVersion());
    }

    @Test
    void testOnConclusion_qualifiedLeavesOpTaskRunning() {
        OpTaskDO opTask = insertRunningTask();

        handler.onConclusion(qcTask(opTask, MesQcConclusionEnum.QUALIFIED));

        OpTaskDO unchanged = opTaskMapper.selectById(opTask.getId());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), unchanged.getStatus());
        assertEquals(MesConstants.INIT_VERSION, unchanged.getVersion());
    }

    private OpTaskDO insertRunningTask() {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-FAI-001")
                .workOrderId(1L)
                .workOrderNo("WO-001")
                .routeOpId(1L)
                .opId(1L)
                .opCode("OP-001")
                .opName("首件工序")
                .seq(10)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .keyFlag(false)
                .plannedQty(java.math.BigDecimal.ONE)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .status(MesOpTaskStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

    private QcTaskDO qcTask(OpTaskDO opTask, MesQcConclusionEnum conclusion) {
        return QcTaskDO.builder().bizNo(opTask.getTaskNo()).conclusion(conclusion.name()).build();
    }

}
