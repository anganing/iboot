package com.iboot.pf.module.aps.service.projectionmat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectionmat.ProjectionMatDO;
import com.iboot.pf.module.aps.dal.mysql.projectionmat.ProjectionMatMapper;
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
 * {@link ProjectionMatServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProjectionMatServiceImpl.class)
public class ProjectionMatServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProjectionMatServiceImpl projectionMatService;

    @Resource
    private ProjectionMatMapper projectionMatMapper;

    @Test
    public void testCreateProjectionMat_success() {
        // 准备参数
        ProjectionMatSaveReqVO createReqVO = randomPojo(ProjectionMatSaveReqVO.class).setId(null);

        // 调用
        Long projectionMatId = projectionMatService.createProjectionMat(createReqVO);
        // 断言
        assertNotNull(projectionMatId);
        // 校验记录的属性是否正确
        ProjectionMatDO projectionMat = projectionMatMapper.selectById(projectionMatId);
        assertPojoEquals(createReqVO, projectionMat, "id");
    }

    @Test
    public void testUpdateProjectionMat_success() {
        // mock 数据
        ProjectionMatDO dbProjectionMat = randomPojo(ProjectionMatDO.class);
        projectionMatMapper.insert(dbProjectionMat);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProjectionMatSaveReqVO updateReqVO = randomPojo(ProjectionMatSaveReqVO.class, o -> {
            o.setId(dbProjectionMat.getId()); // 设置更新的 ID
        });

        // 调用
        projectionMatService.updateProjectionMat(updateReqVO);
        // 校验是否更新正确
        ProjectionMatDO projectionMat = projectionMatMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, projectionMat);
    }

    @Test
    public void testUpdateProjectionMat_notExists() {
        // 准备参数
        ProjectionMatSaveReqVO updateReqVO = randomPojo(ProjectionMatSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> projectionMatService.updateProjectionMat(updateReqVO), PROJECTION_MAT_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectionMat_success() {
        // mock 数据
        ProjectionMatDO dbProjectionMat = randomPojo(ProjectionMatDO.class);
        projectionMatMapper.insert(dbProjectionMat);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProjectionMat.getId();

        // 调用
        projectionMatService.deleteProjectionMat(id);
       // 校验数据不存在了
       assertNull(projectionMatMapper.selectById(id));
    }

    @Test
    public void testDeleteProjectionMat_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> projectionMatService.deleteProjectionMat(id), PROJECTION_MAT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectionMatPage() {
       // mock 数据
       ProjectionMatDO dbProjectionMat = randomPojo(ProjectionMatDO.class, o -> { // 等会查询到
           o.setScenarioId(null);
           o.setSourceMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setMatSpec(null);
           o.setMatType(null);
           o.setBaseUomId(null);
           o.setBaseUomCode(null);
           o.setBatchFlag(null);
           o.setBomId(null);
           o.setBomVersion(null);
           o.setRouteId(null);
           o.setRouteVersion(null);
           o.setSourceUpdateTime(null);
           o.setSnapshotTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       projectionMatMapper.insert(dbProjectionMat);
       // 测试 scenarioId 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setScenarioId(null)));
       // 测试 sourceMatId 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setSourceMatId(null)));
       // 测试 matCode 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setMatName(null)));
       // 测试 matSpec 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setMatSpec(null)));
       // 测试 matType 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setMatType(null)));
       // 测试 baseUomId 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setBaseUomId(null)));
       // 测试 baseUomCode 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setBaseUomCode(null)));
       // 测试 batchFlag 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setBatchFlag(null)));
       // 测试 bomId 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setBomId(null)));
       // 测试 bomVersion 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setBomVersion(null)));
       // 测试 routeId 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setRouteId(null)));
       // 测试 routeVersion 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setRouteVersion(null)));
       // 测试 sourceUpdateTime 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setSourceUpdateTime(null)));
       // 测试 snapshotTime 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setSnapshotTime(null)));
       // 测试 remark 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       projectionMatMapper.insert(cloneIgnoreId(dbProjectionMat, o -> o.setCreateTime(null)));
       // 准备参数
       ProjectionMatPageReqVO reqVO = new ProjectionMatPageReqVO();
       reqVO.setScenarioId(null);
       reqVO.setSourceMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setMatSpec(null);
       reqVO.setMatType(null);
       reqVO.setBaseUomId(null);
       reqVO.setBaseUomCode(null);
       reqVO.setBatchFlag(null);
       reqVO.setBomId(null);
       reqVO.setBomVersion(null);
       reqVO.setRouteId(null);
       reqVO.setRouteVersion(null);
       reqVO.setSourceUpdateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProjectionMatRespVO> pageResult = projectionMatService.getProjectionMatPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProjectionMat, pageResult.getList().get(0));
    }

}