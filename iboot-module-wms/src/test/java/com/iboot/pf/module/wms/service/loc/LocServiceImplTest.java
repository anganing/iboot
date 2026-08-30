package com.iboot.pf.module.wms.service.loc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.loc.LocDO;
import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;
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
 * {@link LocServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(LocServiceImpl.class)
public class LocServiceImplTest extends BaseDbUnitTest {

    @Resource
    private LocServiceImpl locService;

    @Resource
    private LocMapper locMapper;

    @Test
    public void testCreateLoc_success() {
        // 准备参数
        LocSaveReqVO createReqVO = randomPojo(LocSaveReqVO.class).setId(null);

        // 调用
        Long locId = locService.createLoc(createReqVO);
        // 断言
        assertNotNull(locId);
        // 校验记录的属性是否正确
        LocDO loc = locMapper.selectById(locId);
        assertPojoEquals(createReqVO, loc, "id");
    }

    @Test
    public void testUpdateLoc_success() {
        // mock 数据
        LocDO dbLoc = randomPojo(LocDO.class);
        locMapper.insert(dbLoc);// @Sql: 先插入出一条存在的数据
        // 准备参数
        LocSaveReqVO updateReqVO = randomPojo(LocSaveReqVO.class, o -> {
            o.setId(dbLoc.getId()); // 设置更新的 ID
        });

        // 调用
        locService.updateLoc(updateReqVO);
        // 校验是否更新正确
        LocDO loc = locMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, loc);
    }

    @Test
    public void testUpdateLoc_notExists() {
        // 准备参数
        LocSaveReqVO updateReqVO = randomPojo(LocSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> locService.updateLoc(updateReqVO), LOC_NOT_EXISTS);
    }

    @Test
    public void testDeleteLoc_success() {
        // mock 数据
        LocDO dbLoc = randomPojo(LocDO.class);
        locMapper.insert(dbLoc);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbLoc.getId();

        // 调用
        locService.deleteLoc(id);
       // 校验数据不存在了
       assertNull(locMapper.selectById(id));
    }

    @Test
    public void testDeleteLoc_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> locService.deleteLoc(id), LOC_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetLocPage() {
       // mock 数据
       LocDO dbLoc = randomPojo(LocDO.class, o -> { // 等会查询到
           o.setWhId(null);
           o.setZoneId(null);
           o.setCode(null);
           o.setLocType(null);
           o.setAisleNo(null);
           o.setRowNo(null);
           o.setColNo(null);
           o.setLevelNo(null);
           o.setDepthNo(null);
           o.setMaxLength(null);
           o.setMaxWidth(null);
           o.setMaxHeight(null);
           o.setMaxWeight(null);
           o.setMaxVolume(null);
           o.setMaxContainerQty(null);
           o.setDimensionCheckMode(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       locMapper.insert(dbLoc);
       // 测试 whId 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setWhId(null)));
       // 测试 zoneId 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setZoneId(null)));
       // 测试 code 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setCode(null)));
       // 测试 locType 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setLocType(null)));
       // 测试 aisleNo 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setAisleNo(null)));
       // 测试 rowNo 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setRowNo(null)));
       // 测试 colNo 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setColNo(null)));
       // 测试 levelNo 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setLevelNo(null)));
       // 测试 depthNo 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setDepthNo(null)));
       // 测试 maxLength 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxLength(null)));
       // 测试 maxWidth 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxWidth(null)));
       // 测试 maxHeight 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxHeight(null)));
       // 测试 maxWeight 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxWeight(null)));
       // 测试 maxVolume 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxVolume(null)));
       // 测试 maxContainerQty 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setMaxContainerQty(null)));
       // 测试 dimensionCheckMode 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setDimensionCheckMode(null)));
       // 测试 status 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       locMapper.insert(cloneIgnoreId(dbLoc, o -> o.setCreateTime(null)));
       // 准备参数
       LocPageReqVO reqVO = new LocPageReqVO();
       reqVO.setWhId(null);
       reqVO.setZoneId(null);
       reqVO.setCode(null);
       reqVO.setLocType(null);
       reqVO.setAisleNo(null);
       reqVO.setRowNo(null);
       reqVO.setColNo(null);
       reqVO.setLevelNo(null);
       reqVO.setDepthNo(null);
       reqVO.setMaxLength(null);
       reqVO.setMaxWidth(null);
       reqVO.setMaxHeight(null);
       reqVO.setMaxWeight(null);
       reqVO.setMaxVolume(null);
       reqVO.setMaxContainerQty(null);
       reqVO.setDimensionCheckMode(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<LocRespVO> pageResult = locService.getLocPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbLoc, pageResult.getList().get(0));
    }

}