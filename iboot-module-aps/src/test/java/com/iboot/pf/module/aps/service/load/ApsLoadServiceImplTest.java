package com.iboot.pf.module.aps.service.load;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.load.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import com.iboot.pf.module.aps.dal.mysql.load.ApsLoadMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ApsLoadServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ApsLoadServiceImpl.class)
public class ApsLoadServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsLoadServiceImpl loadService;

    @Resource
    private ApsLoadMapper loadMapper;

    @Test
    public void testCreateLoad_success() {
        // 准备参数
        LoadSaveReqVO createReqVO = randomPojo(LoadSaveReqVO.class).setId(null);

        // 调用
        Long loadId = loadService.createLoad(createReqVO);
        // 断言
        assertNotNull(loadId);
        // 校验记录的属性是否正确
        ApsLoadDO load = loadMapper.selectById(loadId);
        assertPojoEquals(createReqVO, load, "id");
    }

    @Test
    public void testUpdateLoad_success() {
        // mock 数据
        ApsLoadDO dbLoad = randomPojo(ApsLoadDO.class);
        loadMapper.insert(dbLoad);// @Sql: 先插入出一条存在的数据
        // 准备参数
        LoadSaveReqVO updateReqVO = randomPojo(LoadSaveReqVO.class, o -> {
            o.setId(dbLoad.getId()); // 设置更新的 ID
        });

        // 调用
        loadService.updateLoad(updateReqVO);
        // 校验是否更新正确
        ApsLoadDO load = loadMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, load);
    }

    @Test
    public void testUpdateLoad_notExists() {
        // 准备参数
        LoadSaveReqVO updateReqVO = randomPojo(LoadSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> loadService.updateLoad(updateReqVO), LOAD_NOT_EXISTS);
    }

    @Test
    public void testDeleteLoad_success() {
        // mock 数据
        ApsLoadDO dbLoad = randomPojo(ApsLoadDO.class);
        loadMapper.insert(dbLoad);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbLoad.getId();

        // 调用
        loadService.deleteLoad(id);
       // 校验数据不存在了
       assertNull(loadMapper.selectById(id));
    }

    @Test
    public void testDeleteLoad_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> loadService.deleteLoad(id), LOAD_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetLoadPage() {
       // mock 数据
       ApsLoadDO dbLoad = randomPojo(ApsLoadDO.class, o -> { // 等会查询到
           o.setVersionId(null);
           o.setResId(null);
           o.setResCode(null);
           o.setResName(null);
           o.setWorkDate(null);
           o.setLoadMinutes(null);
           o.setCapacityMinutes(null);
           o.setLoadRate(null);
           o.setOverloadMinutes(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       loadMapper.insert(dbLoad);
       // 测试 versionId 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setVersionId(null)));
       // 测试 resId 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setResId(null)));
       // 测试 resCode 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setResCode(null)));
       // 测试 resName 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setResName(null)));
       // 测试 workDate 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setWorkDate(null)));
       // 测试 loadMinutes 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setLoadMinutes(null)));
       // 测试 capacityMinutes 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setCapacityMinutes(null)));
       // 测试 loadRate 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setLoadRate(null)));
       // 测试 overloadMinutes 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setOverloadMinutes(null)));
       // 测试 remark 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       loadMapper.insert(cloneIgnoreId(dbLoad, o -> o.setCreateTime(null)));
       // 准备参数
       LoadPageReqVO reqVO = new LoadPageReqVO();
       reqVO.setVersionId(null);
       reqVO.setResId(null);
       reqVO.setResCode(null);
       reqVO.setResName(null);
       reqVO.setWorkDate(new java.time.LocalDate[]{java.time.LocalDate.of(2023, 2, 1), java.time.LocalDate.of(2023, 2, 28)});
       reqVO.setLoadMinutes(null);
       reqVO.setCapacityMinutes(null);
       reqVO.setLoadRate(null);
       reqVO.setOverloadMinutes(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<LoadRespVO> pageResult = loadService.getLoadPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbLoad, pageResult.getList().get(0));
    }

}