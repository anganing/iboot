package com.iboot.pf.module.wms.service.wh;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.wh.WhDO;
import com.iboot.pf.module.wms.dal.mysql.wh.WhMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link WhServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(WhServiceImpl.class)
public class WhServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WhServiceImpl whService;

    @Resource
    private WhMapper whMapper;

    @Test
    public void testCreateWh_success() {
        // 准备参数
        WhSaveReqVO createReqVO = randomPojo(WhSaveReqVO.class).setId(null);

        // 调用
        Long whId = whService.createWh(createReqVO);
        // 断言
        assertNotNull(whId);
        // 校验记录的属性是否正确
        WhDO wh = whMapper.selectById(whId);
        assertPojoEquals(createReqVO, wh, "id");
    }

    @Test
    public void testUpdateWh_success() {
        // mock 数据
        WhDO dbWh = randomPojo(WhDO.class);
        whMapper.insert(dbWh);// @Sql: 先插入出一条存在的数据
        // 准备参数
        WhSaveReqVO updateReqVO = randomPojo(WhSaveReqVO.class, o -> {
            o.setId(dbWh.getId()); // 设置更新的 ID
        });

        // 调用
        whService.updateWh(updateReqVO);
        // 校验是否更新正确
        WhDO wh = whMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, wh);
    }

    @Test
    public void testUpdateWh_notExists() {
        // 准备参数
        WhSaveReqVO updateReqVO = randomPojo(WhSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> whService.updateWh(updateReqVO), WH_NOT_EXISTS);
    }

    @Test
    public void testDeleteWh_success() {
        // mock 数据
        WhDO dbWh = randomPojo(WhDO.class);
        whMapper.insert(dbWh);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbWh.getId();

        // 调用
        whService.deleteWh(id);
       // 校验数据不存在了
       assertNull(whMapper.selectById(id));
    }

    @Test
    public void testDeleteWh_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> whService.deleteWh(id), WH_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetWhPage() {
       // mock 数据
       WhDO dbWh = randomPojo(WhDO.class, o -> { // 等会查询到
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setCode(null);
           o.setName(null);
           o.setWhType(null);
           o.setManagedBy(null);
           o.setExternalSystemCode(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       whMapper.insert(dbWh);
       // 测试 invOrgId 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setPlantId(null)));
       // 测试 code 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setCode(null)));
       // 测试 name 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setName(null)));
       // 测试 whType 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setWhType(null)));
       // 测试 managedBy 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setManagedBy(null)));
       // 测试 externalSystemCode 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setExternalSystemCode(null)));
       // 测试 status 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       whMapper.insert(cloneIgnoreId(dbWh, o -> o.setCreateTime(null)));
       // 准备参数
       WhPageReqVO reqVO = new WhPageReqVO();
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setWhType(null);
       reqVO.setManagedBy(null);
       reqVO.setExternalSystemCode(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<WhRespVO> pageResult = whService.getWhPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbWh, pageResult.getList().get(0));
    }

}