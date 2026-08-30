package com.iboot.pf.module.aps.service.shortage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.shortage.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ShortageDO;
import com.iboot.pf.module.aps.dal.mysql.shortage.ShortageMapper;
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
 * {@link ShortageServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ShortageServiceImpl.class)
public class ShortageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ShortageServiceImpl shortageService;

    @Resource
    private ShortageMapper shortageMapper;

    @Test
    public void testCreateShortage_success() {
        // 准备参数
        ShortageSaveReqVO createReqVO = randomPojo(ShortageSaveReqVO.class).setId(null);

        // 调用
        Long shortageId = shortageService.createShortage(createReqVO);
        // 断言
        assertNotNull(shortageId);
        // 校验记录的属性是否正确
        ShortageDO shortage = shortageMapper.selectById(shortageId);
        assertPojoEquals(createReqVO, shortage, "id");
    }

    @Test
    public void testUpdateShortage_success() {
        // mock 数据
        ShortageDO dbShortage = randomPojo(ShortageDO.class);
        shortageMapper.insert(dbShortage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ShortageSaveReqVO updateReqVO = randomPojo(ShortageSaveReqVO.class, o -> {
            o.setId(dbShortage.getId()); // 设置更新的 ID
        });

        // 调用
        shortageService.updateShortage(updateReqVO);
        // 校验是否更新正确
        ShortageDO shortage = shortageMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, shortage);
    }

    @Test
    public void testUpdateShortage_notExists() {
        // 准备参数
        ShortageSaveReqVO updateReqVO = randomPojo(ShortageSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> shortageService.updateShortage(updateReqVO), SHORTAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteShortage_success() {
        // mock 数据
        ShortageDO dbShortage = randomPojo(ShortageDO.class);
        shortageMapper.insert(dbShortage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbShortage.getId();

        // 调用
        shortageService.deleteShortage(id);
       // 校验数据不存在了
       assertNull(shortageMapper.selectById(id));
    }

    @Test
    public void testDeleteShortage_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> shortageService.deleteShortage(id), SHORTAGE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetShortagePage() {
       // mock 数据
       ShortageDO dbShortage = randomPojo(ShortageDO.class, o -> { // 等会查询到
           o.setVersionId(null);
           o.setProdOrderId(null);
           o.setOpTaskId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setReqQty(null);
           o.setAvailQty(null);
           o.setShortQty(null);
           o.setUomId(null);
           o.setUomCode(null);
           o.setNeedDate(null);
           o.setExpectedSupplyDate(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       shortageMapper.insert(dbShortage);
       // 测试 versionId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setVersionId(null)));
       // 测试 prodOrderId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setProdOrderId(null)));
       // 测试 opTaskId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setOpTaskId(null)));
       // 测试 projectId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setProjectTaskId(null)));
       // 测试 matId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setMatName(null)));
       // 测试 reqQty 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setReqQty(null)));
       // 测试 availQty 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setAvailQty(null)));
       // 测试 shortQty 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setShortQty(null)));
       // 测试 uomId 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setUomId(null)));
       // 测试 uomCode 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setUomCode(null)));
       // 测试 needDate 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setNeedDate(null)));
       // 测试 expectedSupplyDate 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setExpectedSupplyDate(null)));
       // 测试 status 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       shortageMapper.insert(cloneIgnoreId(dbShortage, o -> o.setCreateTime(null)));
       // 准备参数
       ShortagePageReqVO reqVO = new ShortagePageReqVO();
       reqVO.setVersionId(null);
       reqVO.setProdOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setReqQty(null);
       reqVO.setAvailQty(null);
       reqVO.setShortQty(null);
       reqVO.setUomId(null);
       reqVO.setUomCode(null);
       reqVO.setNeedDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setExpectedSupplyDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ShortageRespVO> pageResult = shortageService.getShortagePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbShortage, pageResult.getList().get(0));
    }

}