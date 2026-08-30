package com.iboot.pf.module.aps.service.version;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.version.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.version.VersionDO;
import com.iboot.pf.module.aps.dal.mysql.version.VersionMapper;
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
 * {@link VersionServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(VersionServiceImpl.class)
public class VersionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private VersionServiceImpl versionService;

    @Resource
    private VersionMapper versionMapper;

    @Test
    public void testCreateVersion_success() {
        // 准备参数
        VersionSaveReqVO createReqVO = randomPojo(VersionSaveReqVO.class).setId(null);

        // 调用
        Long versionId = versionService.createVersion(createReqVO);
        // 断言
        assertNotNull(versionId);
        // 校验记录的属性是否正确
        VersionDO version = versionMapper.selectById(versionId);
        assertPojoEquals(createReqVO, version, "id");
    }

    @Test
    public void testUpdateVersion_success() {
        // mock 数据
        VersionDO dbVersion = randomPojo(VersionDO.class);
        versionMapper.insert(dbVersion);// @Sql: 先插入出一条存在的数据
        // 准备参数
        VersionSaveReqVO updateReqVO = randomPojo(VersionSaveReqVO.class, o -> {
            o.setId(dbVersion.getId()); // 设置更新的 ID
        });

        // 调用
        versionService.updateVersion(updateReqVO);
        // 校验是否更新正确
        VersionDO version = versionMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, version);
    }

    @Test
    public void testUpdateVersion_notExists() {
        // 准备参数
        VersionSaveReqVO updateReqVO = randomPojo(VersionSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> versionService.updateVersion(updateReqVO), VERSION_NOT_EXISTS);
    }

    @Test
    public void testDeleteVersion_success() {
        // mock 数据
        VersionDO dbVersion = randomPojo(VersionDO.class);
        versionMapper.insert(dbVersion);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbVersion.getId();

        // 调用
        versionService.deleteVersion(id);
       // 校验数据不存在了
       assertNull(versionMapper.selectById(id));
    }

    @Test
    public void testDeleteVersion_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> versionService.deleteVersion(id), VERSION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetVersionPage() {
       // mock 数据
       VersionDO dbVersion = randomPojo(VersionDO.class, o -> { // 等会查询到
           o.setScenarioId(null);
           o.setVersionNo(null);
           o.setStatus(null);
           o.setScore(null);
           o.setHardScore(null);
           o.setSoftScore(null);
           o.setSolveStartTime(null);
           o.setSolveEndTime(null);
           o.setPublishedTime(null);
           o.setPublishedBy(null);
           o.setSolverMessage(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       versionMapper.insert(dbVersion);
       // 测试 scenarioId 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setScenarioId(null)));
       // 测试 versionNo 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setVersionNo(null)));
       // 测试 status 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setStatus(null)));
       // 测试 score 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setScore(null)));
       // 测试 hardScore 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setHardScore(null)));
       // 测试 softScore 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setSoftScore(null)));
       // 测试 solveStartTime 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setSolveStartTime(null)));
       // 测试 solveEndTime 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setSolveEndTime(null)));
       // 测试 publishedTime 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setPublishedTime(null)));
       // 测试 publishedBy 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setPublishedBy(null)));
       // 测试 solverMessage 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setSolverMessage(null)));
       // 测试 version 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       versionMapper.insert(cloneIgnoreId(dbVersion, o -> o.setCreateTime(null)));
       // 准备参数
       VersionPageReqVO reqVO = new VersionPageReqVO();
       reqVO.setScenarioId(null);
       reqVO.setVersionNo(null);
       reqVO.setStatus(null);
       reqVO.setScore(null);
       reqVO.setHardScore(null);
       reqVO.setSoftScore(null);
       reqVO.setSolveStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSolveEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPublishedTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPublishedBy(null);
       reqVO.setSolverMessage(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<VersionRespVO> pageResult = versionService.getVersionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbVersion, pageResult.getList().get(0));
    }

}