package com.iboot.pf.module.aps.service.projectionresource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionresource.ProjectionResourceDO;
import com.iboot.pf.module.aps.dal.mysql.projectionresource.ProjectionResourceMapper;
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
 * {@link ProjectionResourceServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProjectionResourceServiceImpl.class)
public class ProjectionResourceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProjectionResourceServiceImpl projectionResourceService;

    @Resource
    private ProjectionResourceMapper projectionResourceMapper;

    @Test
    public void testCreateProjectionResource_success() {
        // 准备参数
        ProjectionResourceSaveReqVO createReqVO = randomPojo(ProjectionResourceSaveReqVO.class).setId(null);

        // 调用
        Long projectionResourceId = projectionResourceService.createProjectionResource(createReqVO);
        // 断言
        assertNotNull(projectionResourceId);
        // 校验记录的属性是否正确
        ProjectionResourceDO projectionResource = projectionResourceMapper.selectById(projectionResourceId);
        assertPojoEquals(createReqVO, projectionResource, "id");
    }

    @Test
    public void testUpdateProjectionResource_success() {
        // mock 数据
        ProjectionResourceDO dbProjectionResource = randomPojo(ProjectionResourceDO.class);
        projectionResourceMapper.insert(dbProjectionResource);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProjectionResourceSaveReqVO updateReqVO = randomPojo(ProjectionResourceSaveReqVO.class, o -> {
            o.setId(dbProjectionResource.getId()); // 设置更新的 ID
        });

        // 调用
        projectionResourceService.updateProjectionResource(updateReqVO);
        // 校验是否更新正确
        ProjectionResourceDO projectionResource = projectionResourceMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, projectionResource);
    }

    @Test
    public void testUpdateProjectionResource_notExists() {
        // 准备参数
        ProjectionResourceSaveReqVO updateReqVO = randomPojo(ProjectionResourceSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> projectionResourceService.updateProjectionResource(updateReqVO), PROJECTION_RESOURCE_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectionResource_success() {
        // mock 数据
        ProjectionResourceDO dbProjectionResource = randomPojo(ProjectionResourceDO.class);
        projectionResourceMapper.insert(dbProjectionResource);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProjectionResource.getId();

        // 调用
        projectionResourceService.deleteProjectionResource(id);
       // 校验数据不存在了
       assertNull(projectionResourceMapper.selectById(id));
    }

    @Test
    public void testDeleteProjectionResource_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> projectionResourceService.deleteProjectionResource(id), PROJECTION_RESOURCE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectionResourcePage() {
       // mock 数据
       ProjectionResourceDO dbProjectionResource = randomPojo(ProjectionResourceDO.class, o -> { // 等会查询到
           o.setScenarioId(null);
           o.setSourceResId(null);
           o.setResCode(null);
           o.setResName(null);
           o.setResType(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setWcId(null);
           o.setWorkDate(null);
           o.setShiftId(null);
           o.setAvailableMinutes(null);
           o.setCapabilityJson(null);
           o.setSourceUpdateTime(null);
           o.setSnapshotTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       projectionResourceMapper.insert(dbProjectionResource);
       // 测试 scenarioId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setScenarioId(null)));
       // 测试 sourceResId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setSourceResId(null)));
       // 测试 resCode 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setResCode(null)));
       // 测试 resName 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setResName(null)));
       // 测试 resType 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setResType(null)));
       // 测试 plantId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setWorkshopId(null)));
       // 测试 wcId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setWcId(null)));
       // 测试 workDate 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setWorkDate(null)));
       // 测试 shiftId 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setShiftId(null)));
       // 测试 availableMinutes 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setAvailableMinutes(null)));
       // 测试 capabilityJson 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setCapabilityJson(null)));
       // 测试 sourceUpdateTime 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setSourceUpdateTime(null)));
       // 测试 snapshotTime 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setSnapshotTime(null)));
       // 测试 remark 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       projectionResourceMapper.insert(cloneIgnoreId(dbProjectionResource, o -> o.setCreateTime(null)));
       // 准备参数
       ProjectionResourcePageReqVO reqVO = new ProjectionResourcePageReqVO();
       reqVO.setScenarioId(null);
       reqVO.setSourceResId(null);
       reqVO.setResCode(null);
       reqVO.setResName(null);
       reqVO.setResType(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setWcId(null);
       reqVO.setWorkDate(new java.time.LocalDate[]{java.time.LocalDate.of(2023, 2, 1), java.time.LocalDate.of(2023, 2, 28)});
       reqVO.setShiftId(null);
       reqVO.setAvailableMinutes(null);
       reqVO.setCapabilityJson(null);
       reqVO.setSourceUpdateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProjectionResourceRespVO> pageResult = projectionResourceService.getProjectionResourcePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProjectionResource, pageResult.getList().get(0));
    }

}
