package com.iboot.pf.module.aps.service.projectioninv;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.projectioninv.ProjectionInvDO;
import com.iboot.pf.module.aps.dal.mysql.projectioninv.ProjectionInvMapper;
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
 * {@link ProjectionInvServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProjectionInvServiceImpl.class)
public class ProjectionInvServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProjectionInvServiceImpl projectionInvService;

    @Resource
    private ProjectionInvMapper projectionInvMapper;

    @Test
    public void testCreateProjectionInv_success() {
        // 准备参数
        ProjectionInvSaveReqVO createReqVO = randomPojo(ProjectionInvSaveReqVO.class).setId(null);

        // 调用
        Long projectionInvId = projectionInvService.createProjectionInv(createReqVO);
        // 断言
        assertNotNull(projectionInvId);
        // 校验记录的属性是否正确
        ProjectionInvDO projectionInv = projectionInvMapper.selectById(projectionInvId);
        assertPojoEquals(createReqVO, projectionInv, "id");
    }

    @Test
    public void testUpdateProjectionInv_success() {
        // mock 数据
        ProjectionInvDO dbProjectionInv = randomPojo(ProjectionInvDO.class);
        projectionInvMapper.insert(dbProjectionInv);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProjectionInvSaveReqVO updateReqVO = randomPojo(ProjectionInvSaveReqVO.class, o -> {
            o.setId(dbProjectionInv.getId()); // 设置更新的 ID
        });

        // 调用
        projectionInvService.updateProjectionInv(updateReqVO);
        // 校验是否更新正确
        ProjectionInvDO projectionInv = projectionInvMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, projectionInv);
    }

    @Test
    public void testUpdateProjectionInv_notExists() {
        // 准备参数
        ProjectionInvSaveReqVO updateReqVO = randomPojo(ProjectionInvSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> projectionInvService.updateProjectionInv(updateReqVO), PROJECTION_INV_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectionInv_success() {
        // mock 数据
        ProjectionInvDO dbProjectionInv = randomPojo(ProjectionInvDO.class);
        projectionInvMapper.insert(dbProjectionInv);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProjectionInv.getId();

        // 调用
        projectionInvService.deleteProjectionInv(id);
       // 校验数据不存在了
       assertNull(projectionInvMapper.selectById(id));
    }

    @Test
    public void testDeleteProjectionInv_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> projectionInvService.deleteProjectionInv(id), PROJECTION_INV_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectionInvPage() {
       // mock 数据
       ProjectionInvDO dbProjectionInv = randomPojo(ProjectionInvDO.class, o -> { // 等会查询到
           o.setScenarioId(null);
           o.setSourceBalanceId(null);
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setWhId(null);
           o.setLocId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setBatchId(null);
           o.setLotNo(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setStockScope(null);
           o.setQualityStatus(null);
           o.setQtyOnhand(null);
           o.setQtyReserved(null);
           o.setQtyFrozen(null);
           o.setQtyAvailable(null);
           o.setSourceUpdateTime(null);
           o.setSnapshotTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       projectionInvMapper.insert(dbProjectionInv);
       // 测试 scenarioId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setScenarioId(null)));
       // 测试 sourceBalanceId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setSourceBalanceId(null)));
       // 测试 invOrgId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setPlantId(null)));
       // 测试 whId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setWhId(null)));
       // 测试 locId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setLocId(null)));
       // 测试 matId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setMatCode(null)));
       // 测试 batchId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setBatchId(null)));
       // 测试 lotNo 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setLotNo(null)));
       // 测试 projectId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setProjectTaskId(null)));
       // 测试 stockScope 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setStockScope(null)));
       // 测试 qualityStatus 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setQualityStatus(null)));
       // 测试 qtyOnhand 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setQtyOnhand(null)));
       // 测试 qtyReserved 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setQtyReserved(null)));
       // 测试 qtyFrozen 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setQtyFrozen(null)));
       // 测试 qtyAvailable 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setQtyAvailable(null)));
       // 测试 sourceUpdateTime 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setSourceUpdateTime(null)));
       // 测试 snapshotTime 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setSnapshotTime(null)));
       // 测试 remark 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       projectionInvMapper.insert(cloneIgnoreId(dbProjectionInv, o -> o.setCreateTime(null)));
       // 准备参数
       ProjectionInvPageReqVO reqVO = new ProjectionInvPageReqVO();
       reqVO.setScenarioId(null);
       reqVO.setSourceBalanceId(null);
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setWhId(null);
       reqVO.setLocId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setBatchId(null);
       reqVO.setLotNo(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setStockScope(null);
       reqVO.setQualityStatus(null);
       reqVO.setQtyOnhand(null);
       reqVO.setQtyReserved(null);
       reqVO.setQtyFrozen(null);
       reqVO.setQtyAvailable(null);
       reqVO.setSourceUpdateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProjectionInvRespVO> pageResult = projectionInvService.getProjectionInvPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProjectionInv, pageResult.getList().get(0));
    }

}