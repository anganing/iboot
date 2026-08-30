package com.iboot.pf.module.mes.service.optask;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.*;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OpTaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(OpTaskServiceImpl.class)
public class OpTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OpTaskServiceImpl opTaskService;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Test
    public void testCreateOpTask_success() {
        // 准备参数
        OpTaskSaveReqVO createReqVO = randomPojo(OpTaskSaveReqVO.class).setId(null);

        // 调用
        Long opTaskId = opTaskService.createOpTask(createReqVO);
        // 断言
        assertNotNull(opTaskId);
        // 校验记录的属性是否正确
        OpTaskDO opTask = opTaskMapper.selectById(opTaskId);
        assertPojoEquals(createReqVO, opTask, "id");
    }

    @Test
    public void testUpdateOpTask_success() {
        // mock 数据
        OpTaskDO dbOpTask = randomPojo(OpTaskDO.class);
        opTaskMapper.insert(dbOpTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OpTaskSaveReqVO updateReqVO = randomPojo(OpTaskSaveReqVO.class, o -> {
            o.setId(dbOpTask.getId()); // 设置更新的 ID
        });

        // 调用
        opTaskService.updateOpTask(updateReqVO);
        // 校验是否更新正确
        OpTaskDO opTask = opTaskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, opTask);
    }

    @Test
    public void testUpdateOpTask_notExists() {
        // 准备参数
        OpTaskSaveReqVO updateReqVO = randomPojo(OpTaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> opTaskService.updateOpTask(updateReqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteOpTask_success() {
        // mock 数据
        OpTaskDO dbOpTask = randomPojo(OpTaskDO.class);
        opTaskMapper.insert(dbOpTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOpTask.getId();

        // 调用
        opTaskService.deleteOpTask(id);
       // 校验数据不存在了
       assertNull(opTaskMapper.selectById(id));
    }

    @Test
    public void testDeleteOpTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> opTaskService.deleteOpTask(id), OP_TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOpTaskPage() {
       // mock 数据
       OpTaskDO dbOpTask = randomPojo(OpTaskDO.class, o -> { // 等会查询到
           o.setTaskNo(null);
           o.setWorkOrderId(null);
           o.setWorkOrderNo(null);
           o.setRouteOpId(null);
           o.setOpId(null);
           o.setOpCode(null);
           o.setOpName(null);
           o.setSeq(null);
           o.setParallelGroup(null);
           o.setPrevTaskId(null);
           o.setNextTaskId(null);
           o.setExecMode(null);
           o.setQcMode(null);
           o.setSkipPolicy(null);
           o.setKeyFlag(null);
           o.setPlannedQty(null);
           o.setCompletedQty(null);
           o.setScrapQty(null);
           o.setResId(null);
           o.setPlannedStartTime(null);
           o.setPlannedEndTime(null);
           o.setActualStartTime(null);
           o.setActualEndTime(null);
           o.setStatus(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       opTaskMapper.insert(dbOpTask);
       // 测试 taskNo 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setTaskNo(null)));
       // 测试 workOrderId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setWorkOrderId(null)));
       // 测试 workOrderNo 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setWorkOrderNo(null)));
       // 测试 routeOpId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setRouteOpId(null)));
       // 测试 opId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setOpId(null)));
       // 测试 opCode 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setOpCode(null)));
       // 测试 opName 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setOpName(null)));
       // 测试 seq 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setSeq(null)));
       // 测试 parallelGroup 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setParallelGroup(null)));
       // 测试 prevTaskId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setPrevTaskId(null)));
       // 测试 nextTaskId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setNextTaskId(null)));
       // 测试 execMode 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setExecMode(null)));
       // 测试 qcMode 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setQcMode(null)));
       // 测试 skipPolicy 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setSkipPolicy(null)));
       // 测试 keyFlag 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setKeyFlag(null)));
       // 测试 plannedQty 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setPlannedQty(null)));
       // 测试 completedQty 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setCompletedQty(null)));
       // 测试 scrapQty 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setScrapQty(null)));
       // 测试 resId 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setResId(null)));
       // 测试 plannedStartTime 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setPlannedStartTime(null)));
       // 测试 plannedEndTime 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setPlannedEndTime(null)));
       // 测试 actualStartTime 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setActualStartTime(null)));
       // 测试 actualEndTime 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setActualEndTime(null)));
       // 测试 status 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setStatus(null)));
       // 测试 version 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       opTaskMapper.insert(cloneIgnoreId(dbOpTask, o -> o.setCreateTime(null)));
       // 准备参数
       OpTaskPageReqVO reqVO = new OpTaskPageReqVO();
       reqVO.setTaskNo(null);
       reqVO.setWorkOrderId(null);
       reqVO.setWorkOrderNo(null);
       reqVO.setRouteOpId(null);
       reqVO.setOpId(null);
       reqVO.setOpCode(null);
       reqVO.setOpName(null);
       reqVO.setSeq(null);
       reqVO.setParallelGroup(null);
       reqVO.setPrevTaskId(null);
       reqVO.setNextTaskId(null);
       reqVO.setExecMode(null);
       reqVO.setQcMode(null);
       reqVO.setSkipPolicy(null);
       reqVO.setKeyFlag(null);
       reqVO.setPlannedQty(null);
       reqVO.setCompletedQty(null);
       reqVO.setScrapQty(null);
       reqVO.setResId(null);
       reqVO.setPlannedStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPlannedEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OpTaskRespVO> pageResult = opTaskService.getOpTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOpTask, pageResult.getList().get(0));
    }

}