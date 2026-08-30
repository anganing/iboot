package com.iboot.pf.module.aps.service.solversnapshot;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.solversnapshot.SolverSnapshotDO;
import com.iboot.pf.module.aps.dal.mysql.solversnapshot.SolverSnapshotMapper;
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
 * {@link SolverSnapshotServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(SolverSnapshotServiceImpl.class)
public class SolverSnapshotServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SolverSnapshotServiceImpl solverSnapshotService;

    @Resource
    private SolverSnapshotMapper solverSnapshotMapper;

    @Test
    public void testCreateSolverSnapshot_success() {
        // 准备参数
        SolverSnapshotSaveReqVO createReqVO = randomPojo(SolverSnapshotSaveReqVO.class).setId(null);

        // 调用
        Long solverSnapshotId = solverSnapshotService.createSolverSnapshot(createReqVO);
        // 断言
        assertNotNull(solverSnapshotId);
        // 校验记录的属性是否正确
        SolverSnapshotDO solverSnapshot = solverSnapshotMapper.selectById(solverSnapshotId);
        assertPojoEquals(createReqVO, solverSnapshot, "id");
    }

    @Test
    public void testUpdateSolverSnapshot_success() {
        // mock 数据
        SolverSnapshotDO dbSolverSnapshot = randomPojo(SolverSnapshotDO.class);
        solverSnapshotMapper.insert(dbSolverSnapshot);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SolverSnapshotSaveReqVO updateReqVO = randomPojo(SolverSnapshotSaveReqVO.class, o -> {
            o.setId(dbSolverSnapshot.getId()); // 设置更新的 ID
        });

        // 调用
        solverSnapshotService.updateSolverSnapshot(updateReqVO);
        // 校验是否更新正确
        SolverSnapshotDO solverSnapshot = solverSnapshotMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, solverSnapshot);
    }

    @Test
    public void testUpdateSolverSnapshot_notExists() {
        // 准备参数
        SolverSnapshotSaveReqVO updateReqVO = randomPojo(SolverSnapshotSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> solverSnapshotService.updateSolverSnapshot(updateReqVO), SOLVER_SNAPSHOT_NOT_EXISTS);
    }

    @Test
    public void testDeleteSolverSnapshot_success() {
        // mock 数据
        SolverSnapshotDO dbSolverSnapshot = randomPojo(SolverSnapshotDO.class);
        solverSnapshotMapper.insert(dbSolverSnapshot);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSolverSnapshot.getId();

        // 调用
        solverSnapshotService.deleteSolverSnapshot(id);
       // 校验数据不存在了
       assertNull(solverSnapshotMapper.selectById(id));
    }

    @Test
    public void testDeleteSolverSnapshot_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> solverSnapshotService.deleteSolverSnapshot(id), SOLVER_SNAPSHOT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSolverSnapshotPage() {
       // mock 数据
       SolverSnapshotDO dbSolverSnapshot = randomPojo(SolverSnapshotDO.class, o -> { // 等会查询到
           o.setVersionId(null);
           o.setSnapshotType(null);
           o.setSnapshotJson(null);
           o.setChecksum(null);
           o.setSnapshotTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       solverSnapshotMapper.insert(dbSolverSnapshot);
       // 测试 versionId 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setVersionId(null)));
       // 测试 snapshotType 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setSnapshotType(null)));
       // 测试 snapshotJson 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setSnapshotJson(null)));
       // 测试 checksum 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setChecksum(null)));
       // 测试 snapshotTime 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setSnapshotTime(null)));
       // 测试 remark 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       solverSnapshotMapper.insert(cloneIgnoreId(dbSolverSnapshot, o -> o.setCreateTime(null)));
       // 准备参数
       SolverSnapshotPageReqVO reqVO = new SolverSnapshotPageReqVO();
       reqVO.setVersionId(null);
       reqVO.setSnapshotType(null);
       reqVO.setSnapshotJson(null);
       reqVO.setChecksum(null);
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SolverSnapshotRespVO> pageResult = solverSnapshotService.getSolverSnapshotPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSolverSnapshot, pageResult.getList().get(0));
    }

}