package com.iboot.pf.module.mes.service.workorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.*;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
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
 * {@link WorkOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(WorkOrderServiceImpl.class)
public class WorkOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WorkOrderServiceImpl workOrderService;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Test
    public void testCreateWorkOrder_success() {
        // 准备参数
        WorkOrderSaveReqVO createReqVO = randomPojo(WorkOrderSaveReqVO.class).setId(null);

        // 调用
        Long workOrderId = workOrderService.createWorkOrder(createReqVO);
        // 断言
        assertNotNull(workOrderId);
        // 校验记录的属性是否正确
        WorkOrderDO workOrder = workOrderMapper.selectById(workOrderId);
        assertPojoEquals(createReqVO, workOrder, "id");
    }

    @Test
    public void testUpdateWorkOrder_success() {
        // mock 数据
        WorkOrderDO dbWorkOrder = randomPojo(WorkOrderDO.class);
        workOrderMapper.insert(dbWorkOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        WorkOrderSaveReqVO updateReqVO = randomPojo(WorkOrderSaveReqVO.class, o -> {
            o.setId(dbWorkOrder.getId()); // 设置更新的 ID
        });

        // 调用
        workOrderService.updateWorkOrder(updateReqVO);
        // 校验是否更新正确
        WorkOrderDO workOrder = workOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, workOrder);
    }

    @Test
    public void testUpdateWorkOrder_notExists() {
        // 准备参数
        WorkOrderSaveReqVO updateReqVO = randomPojo(WorkOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> workOrderService.updateWorkOrder(updateReqVO), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteWorkOrder_success() {
        // mock 数据
        WorkOrderDO dbWorkOrder = randomPojo(WorkOrderDO.class);
        workOrderMapper.insert(dbWorkOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbWorkOrder.getId();

        // 调用
        workOrderService.deleteWorkOrder(id);
       // 校验数据不存在了
       assertNull(workOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteWorkOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> workOrderService.deleteWorkOrder(id), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetWorkOrderPage() {
       // mock 数据
       WorkOrderDO dbWorkOrder = randomPojo(WorkOrderDO.class, o -> { // 等会查询到
           o.setWorkOrderNo(null);
           o.setProdOrderId(null);
           o.setProdOrderNo(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setWcId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setPlannedQty(null);
           o.setCompletedQty(null);
           o.setPlannedStartTime(null);
           o.setPlannedEndTime(null);
           o.setActualStartTime(null);
           o.setActualEndTime(null);
           o.setKitStatus(null);
           o.setStatus(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       workOrderMapper.insert(dbWorkOrder);
       // 测试 workOrderNo 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setWorkOrderNo(null)));
       // 测试 prodOrderId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setProdOrderId(null)));
       // 测试 prodOrderNo 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setProdOrderNo(null)));
       // 测试 plantId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setWorkshopId(null)));
       // 测试 wcId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setWcId(null)));
       // 测试 projectId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setProjectTaskId(null)));
       // 测试 plannedQty 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setPlannedQty(null)));
       // 测试 completedQty 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setCompletedQty(null)));
       // 测试 plannedStartTime 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setPlannedStartTime(null)));
       // 测试 plannedEndTime 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setPlannedEndTime(null)));
       // 测试 actualStartTime 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setActualStartTime(null)));
       // 测试 actualEndTime 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setActualEndTime(null)));
       // 测试 kitStatus 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setKitStatus(null)));
       // 测试 status 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setStatus(null)));
       // 测试 version 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       workOrderMapper.insert(cloneIgnoreId(dbWorkOrder, o -> o.setCreateTime(null)));
       // 准备参数
       WorkOrderPageReqVO reqVO = new WorkOrderPageReqVO();
       reqVO.setWorkOrderNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setProdOrderNo(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setWcId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setPlannedQty(null);
       reqVO.setCompletedQty(null);
       reqVO.setPlannedStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPlannedEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setKitStatus(null);
       reqVO.setStatus(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<WorkOrderRespVO> pageResult = workOrderService.getWorkOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbWorkOrder, pageResult.getList().get(0));
    }

}