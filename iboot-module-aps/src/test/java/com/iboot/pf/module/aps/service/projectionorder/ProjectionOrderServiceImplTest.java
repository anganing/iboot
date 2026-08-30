package com.iboot.pf.module.aps.service.projectionorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionorder.ProjectionOrderDO;
import com.iboot.pf.module.aps.dal.mysql.projectionorder.ProjectionOrderMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ProjectionOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProjectionOrderServiceImpl.class)
public class ProjectionOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProjectionOrderServiceImpl projectionOrderService;

    @Resource
    private ProjectionOrderMapper projectionOrderMapper;

    @Test
    public void testCreateProjectionOrder_success() {
        // 准备参数
        ProjectionOrderSaveReqVO createReqVO = randomPojo(ProjectionOrderSaveReqVO.class).setId(null);

        // 调用
        Long projectionOrderId = projectionOrderService.createProjectionOrder(createReqVO);
        // 断言
        assertNotNull(projectionOrderId);
        // 校验记录的属性是否正确
        ProjectionOrderDO projectionOrder = projectionOrderMapper.selectById(projectionOrderId);
        assertPojoEquals(createReqVO, projectionOrder, "id");
    }

    @Test
    public void testUpdateProjectionOrder_success() {
        // mock 数据
        ProjectionOrderDO dbProjectionOrder = randomPojo(ProjectionOrderDO.class);
        projectionOrderMapper.insert(dbProjectionOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProjectionOrderSaveReqVO updateReqVO = randomPojo(ProjectionOrderSaveReqVO.class, o -> {
            o.setId(dbProjectionOrder.getId()); // 设置更新的 ID
        });

        // 调用
        projectionOrderService.updateProjectionOrder(updateReqVO);
        // 校验是否更新正确
        ProjectionOrderDO projectionOrder = projectionOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, projectionOrder);
    }

    @Test
    public void testUpdateProjectionOrder_notExists() {
        // 准备参数
        ProjectionOrderSaveReqVO updateReqVO = randomPojo(ProjectionOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> projectionOrderService.updateProjectionOrder(updateReqVO), PROJECTION_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectionOrder_success() {
        // mock 数据
        ProjectionOrderDO dbProjectionOrder = randomPojo(ProjectionOrderDO.class);
        projectionOrderMapper.insert(dbProjectionOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProjectionOrder.getId();

        // 调用
        projectionOrderService.deleteProjectionOrder(id);
       // 校验数据不存在了
       assertNull(projectionOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteProjectionOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> projectionOrderService.deleteProjectionOrder(id), PROJECTION_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectionOrderPage() {
       // mock 数据
       ProjectionOrderDO dbProjectionOrder = randomPojo(ProjectionOrderDO.class, o -> { // 等会查询到
           o.setScenarioId(null);
           o.setSourceType(null);
           o.setSourceId(null);
           o.setSourceNo(null);
           o.setProdOrderId(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setRouteOpId(null);
           o.setResId(null);
           o.setQty(null);
           o.setCompletedQty(null);
           o.setDueDate(null);
           o.setPriority(null);
           o.setCurrentStatus(null);
           o.setActualStartTime(null);
           o.setActualEndTime(null);
           o.setLockedFlag(null);
           o.setSourceUpdateTime(null);
           o.setSnapshotTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       projectionOrderMapper.insert(dbProjectionOrder);
       // 测试 scenarioId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setScenarioId(null)));
       // 测试 sourceType 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setSourceType(null)));
       // 测试 sourceId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setSourceId(null)));
       // 测试 sourceNo 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setSourceNo(null)));
       // 测试 prodOrderId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setProdOrderId(null)));
       // 测试 workOrderId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setOpTaskId(null)));
       // 测试 plantId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setWorkshopId(null)));
       // 测试 projectId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setProjectTaskId(null)));
       // 测试 matId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setMatCode(null)));
       // 测试 routeOpId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setRouteOpId(null)));
       // 测试 resId 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setResId(null)));
       // 测试 qty 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setQty(null)));
       // 测试 completedQty 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setCompletedQty(null)));
       // 测试 dueDate 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setDueDate(null)));
       // 测试 priority 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setPriority(null)));
       // 测试 currentStatus 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setCurrentStatus(null)));
       // 测试 actualStartTime 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setActualStartTime(null)));
       // 测试 actualEndTime 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setActualEndTime(null)));
       // 测试 lockedFlag 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setLockedFlag(null)));
       // 测试 sourceUpdateTime 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setSourceUpdateTime(null)));
       // 测试 snapshotTime 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setSnapshotTime(null)));
       // 测试 remark 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       projectionOrderMapper.insert(cloneIgnoreId(dbProjectionOrder, o -> o.setCreateTime(null)));
       // 准备参数
       ProjectionOrderPageReqVO reqVO = new ProjectionOrderPageReqVO();
       reqVO.setScenarioId(null);
       reqVO.setSourceType(null);
       reqVO.setSourceId(null);
       reqVO.setSourceNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setRouteOpId(null);
       reqVO.setResId(null);
       reqVO.setQty(null);
       reqVO.setCompletedQty(null);
       reqVO.setDueDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPriority(null);
       reqVO.setCurrentStatus(null);
       reqVO.setActualStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setLockedFlag(null);
       reqVO.setSourceUpdateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProjectionOrderRespVO> pageResult = projectionOrderService.getProjectionOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProjectionOrder, pageResult.getList().get(0));
    }

}