package com.iboot.pf.module.wms.service.zone;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.zone.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.zone.ZoneDO;
import com.iboot.pf.module.wms.dal.mysql.zone.ZoneMapper;
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
 * {@link ZoneServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ZoneServiceImpl.class)
public class ZoneServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ZoneServiceImpl zoneService;

    @Resource
    private ZoneMapper zoneMapper;

    @Test
    public void testCreateZone_success() {
        // 准备参数
        ZoneSaveReqVO createReqVO = randomPojo(ZoneSaveReqVO.class).setId(null);

        // 调用
        Long zoneId = zoneService.createZone(createReqVO);
        // 断言
        assertNotNull(zoneId);
        // 校验记录的属性是否正确
        ZoneDO zone = zoneMapper.selectById(zoneId);
        assertPojoEquals(createReqVO, zone, "id");
    }

    @Test
    public void testUpdateZone_success() {
        // mock 数据
        ZoneDO dbZone = randomPojo(ZoneDO.class);
        zoneMapper.insert(dbZone);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ZoneSaveReqVO updateReqVO = randomPojo(ZoneSaveReqVO.class, o -> {
            o.setId(dbZone.getId()); // 设置更新的 ID
        });

        // 调用
        zoneService.updateZone(updateReqVO);
        // 校验是否更新正确
        ZoneDO zone = zoneMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, zone);
    }

    @Test
    public void testUpdateZone_notExists() {
        // 准备参数
        ZoneSaveReqVO updateReqVO = randomPojo(ZoneSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> zoneService.updateZone(updateReqVO), ZONE_NOT_EXISTS);
    }

    @Test
    public void testDeleteZone_success() {
        // mock 数据
        ZoneDO dbZone = randomPojo(ZoneDO.class);
        zoneMapper.insert(dbZone);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbZone.getId();

        // 调用
        zoneService.deleteZone(id);
       // 校验数据不存在了
       assertNull(zoneMapper.selectById(id));
    }

    @Test
    public void testDeleteZone_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> zoneService.deleteZone(id), ZONE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetZonePage() {
       // mock 数据
       ZoneDO dbZone = randomPojo(ZoneDO.class, o -> { // 等会查询到
           o.setWhId(null);
           o.setCode(null);
           o.setName(null);
           o.setZoneType(null);
           o.setAllowMixedMat(null);
           o.setAllowMixedLot(null);
           o.setTempRange(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       zoneMapper.insert(dbZone);
       // 测试 whId 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setWhId(null)));
       // 测试 code 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setCode(null)));
       // 测试 name 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setName(null)));
       // 测试 zoneType 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setZoneType(null)));
       // 测试 allowMixedMat 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setAllowMixedMat(null)));
       // 测试 allowMixedLot 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setAllowMixedLot(null)));
       // 测试 tempRange 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setTempRange(null)));
       // 测试 status 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       zoneMapper.insert(cloneIgnoreId(dbZone, o -> o.setCreateTime(null)));
       // 准备参数
       ZonePageReqVO reqVO = new ZonePageReqVO();
       reqVO.setWhId(null);
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setZoneType(null);
       reqVO.setAllowMixedMat(null);
       reqVO.setAllowMixedLot(null);
       reqVO.setTempRange(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ZoneRespVO> pageResult = zoneService.getZonePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbZone, pageResult.getList().get(0));
    }

}