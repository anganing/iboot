package com.iboot.pf.module.mes.service.rework;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.*;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
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
 * {@link ReworkServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ReworkServiceImpl.class)
public class ReworkServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ReworkServiceImpl reworkService;

    @Resource
    private ReworkMapper reworkMapper;

    @Test
    public void testCreateRework_success() {
        // 准备参数
        ReworkSaveReqVO createReqVO = randomPojo(ReworkSaveReqVO.class).setId(null);

        // 调用
        Long reworkId = reworkService.createRework(createReqVO);
        // 断言
        assertNotNull(reworkId);
        // 校验记录的属性是否正确
        ReworkDO rework = reworkMapper.selectById(reworkId);
        assertPojoEquals(createReqVO, rework, "id");
    }

    @Test
    public void testUpdateRework_success() {
        // mock 数据
        ReworkDO dbRework = randomPojo(ReworkDO.class);
        reworkMapper.insert(dbRework);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ReworkSaveReqVO updateReqVO = randomPojo(ReworkSaveReqVO.class, o -> {
            o.setId(dbRework.getId()); // 设置更新的 ID
        });

        // 调用
        reworkService.updateRework(updateReqVO);
        // 校验是否更新正确
        ReworkDO rework = reworkMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, rework);
    }

    @Test
    public void testUpdateRework_notExists() {
        // 准备参数
        ReworkSaveReqVO updateReqVO = randomPojo(ReworkSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> reworkService.updateRework(updateReqVO), REWORK_NOT_EXISTS);
    }

    @Test
    public void testDeleteRework_success() {
        // mock 数据
        ReworkDO dbRework = randomPojo(ReworkDO.class);
        reworkMapper.insert(dbRework);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbRework.getId();

        // 调用
        reworkService.deleteRework(id);
       // 校验数据不存在了
       assertNull(reworkMapper.selectById(id));
    }

    @Test
    public void testDeleteRework_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> reworkService.deleteRework(id), REWORK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetReworkPage() {
       // mock 数据
       ReworkDO dbRework = randomPojo(ReworkDO.class, o -> { // 等会查询到
           o.setReworkNo(null);
           o.setSourceQcTaskId(null);
           o.setProdOrderId(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setQty(null);
           o.setRouteId(null);
           o.setRouteVersion(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       reworkMapper.insert(dbRework);
       // 测试 reworkNo 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setReworkNo(null)));
       // 测试 sourceQcTaskId 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setSourceQcTaskId(null)));
       // 测试 prodOrderId 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setProdOrderId(null)));
       // 测试 workOrderId 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setOpTaskId(null)));
       // 测试 qty 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setQty(null)));
       // 测试 routeId 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setRouteId(null)));
       // 测试 routeVersion 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setRouteVersion(null)));
       // 测试 status 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       reworkMapper.insert(cloneIgnoreId(dbRework, o -> o.setCreateTime(null)));
       // 准备参数
       ReworkPageReqVO reqVO = new ReworkPageReqVO();
       reqVO.setReworkNo(null);
       reqVO.setSourceQcTaskId(null);
       reqVO.setProdOrderId(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setQty(null);
       reqVO.setRouteId(null);
       reqVO.setRouteVersion(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ReworkRespVO> pageResult = reworkService.getReworkPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbRework, pageResult.getList().get(0));
    }

}