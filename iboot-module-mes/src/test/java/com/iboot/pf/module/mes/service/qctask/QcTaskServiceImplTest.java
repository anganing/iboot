package com.iboot.pf.module.mes.service.qctask;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.*;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
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
 * {@link QcTaskServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(QcTaskServiceImpl.class)
public class QcTaskServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcTaskServiceImpl qcTaskService;

    @Resource
    private QcTaskMapper qcTaskMapper;

    @Test
    public void testCreateQcTask_success() {
        // 准备参数
        QcTaskSaveReqVO createReqVO = randomPojo(QcTaskSaveReqVO.class).setId(null);

        // 调用
        Long qcTaskId = qcTaskService.createQcTask(createReqVO);
        // 断言
        assertNotNull(qcTaskId);
        // 校验记录的属性是否正确
        QcTaskDO qcTask = qcTaskMapper.selectById(qcTaskId);
        assertPojoEquals(createReqVO, qcTask, "id");
    }

    @Test
    public void testUpdateQcTask_success() {
        // mock 数据
        QcTaskDO dbQcTask = randomPojo(QcTaskDO.class);
        qcTaskMapper.insert(dbQcTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QcTaskSaveReqVO updateReqVO = randomPojo(QcTaskSaveReqVO.class, o -> {
            o.setId(dbQcTask.getId()); // 设置更新的 ID
        });

        // 调用
        qcTaskService.updateQcTask(updateReqVO);
        // 校验是否更新正确
        QcTaskDO qcTask = qcTaskMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, qcTask);
    }

    @Test
    public void testUpdateQcTask_notExists() {
        // 准备参数
        QcTaskSaveReqVO updateReqVO = randomPojo(QcTaskSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> qcTaskService.updateQcTask(updateReqVO), QC_TASK_NOT_EXISTS);
    }

    @Test
    public void testDeleteQcTask_success() {
        // mock 数据
        QcTaskDO dbQcTask = randomPojo(QcTaskDO.class);
        qcTaskMapper.insert(dbQcTask);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQcTask.getId();

        // 调用
        qcTaskService.deleteQcTask(id);
       // 校验数据不存在了
       assertNull(qcTaskMapper.selectById(id));
    }

    @Test
    public void testDeleteQcTask_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> qcTaskService.deleteQcTask(id), QC_TASK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQcTaskPage() {
       // mock 数据
       QcTaskDO dbQcTask = randomPojo(QcTaskDO.class, o -> { // 等会查询到
           o.setTaskNo(null);
           o.setRuleId(null);
           o.setQcPlanId(null);
           o.setQcPlanVersion(null);
           o.setTriggerPoint(null);
           o.setBizType(null);
           o.setBizNo(null);
           o.setBizLineId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setMatSpec(null);
           o.setBatchId(null);
           o.setLotNo(null);
           o.setQty(null);
           o.setSampleQty(null);
           o.setUomId(null);
           o.setUomCode(null);
           o.setAssignedInspectorId(null);
           o.setInspectStartTime(null);
           o.setInspectEndTime(null);
           o.setConclusion(null);
           o.setQtyQualified(null);
           o.setQtyRejected(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       qcTaskMapper.insert(dbQcTask);
       // 测试 taskNo 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setTaskNo(null)));
       // 测试 ruleId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setRuleId(null)));
       // 测试 qcPlanId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setQcPlanId(null)));
       // 测试 qcPlanVersion 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setQcPlanVersion(null)));
       // 测试 triggerPoint 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setTriggerPoint(null)));
       // 测试 bizType 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setBizType(null)));
       // 测试 bizNo 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setBizNo(null)));
       // 测试 bizLineId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setBizLineId(null)));
       // 测试 matId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setMatName(null)));
       // 测试 matSpec 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setMatSpec(null)));
       // 测试 batchId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setBatchId(null)));
       // 测试 lotNo 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setLotNo(null)));
       // 测试 qty 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setQty(null)));
       // 测试 sampleQty 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setSampleQty(null)));
       // 测试 uomId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setUomId(null)));
       // 测试 uomCode 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setUomCode(null)));
       // 测试 assignedInspectorId 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setAssignedInspectorId(null)));
       // 测试 inspectStartTime 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setInspectStartTime(null)));
       // 测试 inspectEndTime 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setInspectEndTime(null)));
       // 测试 conclusion 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setConclusion(null)));
       // 测试 qtyQualified 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setQtyQualified(null)));
       // 测试 qtyRejected 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setQtyRejected(null)));
       // 测试 status 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setIdempotencyKey(null)));
       // 测试 version 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       qcTaskMapper.insert(cloneIgnoreId(dbQcTask, o -> o.setCreateTime(null)));
       // 准备参数
       QcTaskPageReqVO reqVO = new QcTaskPageReqVO();
       reqVO.setTaskNo(null);
       reqVO.setRuleId(null);
       reqVO.setQcPlanId(null);
       reqVO.setQcPlanVersion(null);
       reqVO.setTriggerPoint(null);
       reqVO.setBizType(null);
       reqVO.setBizNo(null);
       reqVO.setBizLineId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setMatSpec(null);
       reqVO.setBatchId(null);
       reqVO.setLotNo(null);
       reqVO.setQty(null);
       reqVO.setSampleQty(null);
       reqVO.setUomId(null);
       reqVO.setUomCode(null);
       reqVO.setAssignedInspectorId(null);
       reqVO.setInspectStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setInspectEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setConclusion(null);
       reqVO.setQtyQualified(null);
       reqVO.setQtyRejected(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QcTaskRespVO> pageResult = qcTaskService.getQcTaskPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQcTask, pageResult.getList().get(0));
    }

}