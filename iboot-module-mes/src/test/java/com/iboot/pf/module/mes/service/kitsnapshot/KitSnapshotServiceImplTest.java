package com.iboot.pf.module.mes.service.kitsnapshot;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.*;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link KitSnapshotServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(KitSnapshotServiceImpl.class)
public class KitSnapshotServiceImplTest extends BaseDbUnitTest {

    @Resource
    private KitSnapshotServiceImpl kitSnapshotService;

    @Resource
    private KitSnapshotMapper kitSnapshotMapper;

    @Test
    public void testCreateKitSnapshot_success() {
        // 准备参数
        KitSnapshotSaveReqVO createReqVO = randomPojo(KitSnapshotSaveReqVO.class).setId(null);

        // 调用
        Long kitSnapshotId = kitSnapshotService.createKitSnapshot(createReqVO);
        // 断言
        assertNotNull(kitSnapshotId);
        // 校验记录的属性是否正确
        KitSnapshotDO kitSnapshot = kitSnapshotMapper.selectById(kitSnapshotId);
        assertPojoEquals(createReqVO, kitSnapshot, "id");
    }

    @Test
    public void testUpdateKitSnapshot_success() {
        // mock 数据
        KitSnapshotDO dbKitSnapshot = randomPojo(KitSnapshotDO.class);
        kitSnapshotMapper.insert(dbKitSnapshot);// @Sql: 先插入出一条存在的数据
        // 准备参数
        KitSnapshotSaveReqVO updateReqVO = randomPojo(KitSnapshotSaveReqVO.class, o -> {
            o.setId(dbKitSnapshot.getId()); // 设置更新的 ID
        });

        // 调用
        kitSnapshotService.updateKitSnapshot(updateReqVO);
        // 校验是否更新正确
        KitSnapshotDO kitSnapshot = kitSnapshotMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, kitSnapshot);
    }

    @Test
    public void testUpdateKitSnapshot_notExists() {
        // 准备参数
        KitSnapshotSaveReqVO updateReqVO = randomPojo(KitSnapshotSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> kitSnapshotService.updateKitSnapshot(updateReqVO), KIT_SNAPSHOT_NOT_EXISTS);
    }

    @Test
    public void testDeleteKitSnapshot_success() {
        // mock 数据
        KitSnapshotDO dbKitSnapshot = randomPojo(KitSnapshotDO.class);
        kitSnapshotMapper.insert(dbKitSnapshot);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbKitSnapshot.getId();

        // 调用
        kitSnapshotService.deleteKitSnapshot(id);
       // 校验数据不存在了
       assertNull(kitSnapshotMapper.selectById(id));
    }

    @Test
    public void testDeleteKitSnapshot_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> kitSnapshotService.deleteKitSnapshot(id), KIT_SNAPSHOT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetKitSnapshotPage() {
       // mock 数据
       KitSnapshotDO dbKitSnapshot = randomPojo(KitSnapshotDO.class, o -> { // 等会查询到
           o.setCheckNo(null);
           o.setCheckScope(null);
           o.setProdOrderId(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setLineNo(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setReqQty(null);
           o.setAvailQty(null);
           o.setReservedQty(null);
           o.setIssuedQty(null);
           o.setShortQty(null);
           o.setAltMatId(null);
           o.setResult(null);
           o.setCheckTime(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       kitSnapshotMapper.insert(dbKitSnapshot);
       // 测试 checkNo 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setCheckNo(null)));
       // 测试 checkScope 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setCheckScope(null)));
       // 测试 prodOrderId 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setProdOrderId(null)));
       // 测试 workOrderId 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setOpTaskId(null)));
       // 测试 lineNo 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setLineNo(null)));
       // 测试 matId 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setMatName(null)));
       // 测试 reqQty 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setReqQty(null)));
       // 测试 availQty 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setAvailQty(null)));
       // 测试 reservedQty 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setReservedQty(null)));
       // 测试 issuedQty 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setIssuedQty(null)));
       // 测试 shortQty 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setShortQty(null)));
       // 测试 altMatId 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setAltMatId(null)));
       // 测试 result 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setResult(null)));
       // 测试 checkTime 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setCheckTime(null)));
       // 测试 remark 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       kitSnapshotMapper.insert(cloneIgnoreId(dbKitSnapshot, o -> o.setCreateTime(null)));
       // 准备参数
       KitSnapshotPageReqVO reqVO = new KitSnapshotPageReqVO();
       reqVO.setCheckNo(null);
       reqVO.setCheckScope(null);
       reqVO.setProdOrderId(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setLineNo(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setReqQty(null);
       reqVO.setAvailQty(null);
       reqVO.setReservedQty(null);
       reqVO.setIssuedQty(null);
       reqVO.setShortQty(null);
       reqVO.setAltMatId(null);
       reqVO.setResult(null);
       reqVO.setCheckTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<KitSnapshotRespVO> pageResult = kitSnapshotService.getKitSnapshotPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbKitSnapshot, pageResult.getList().get(0));
    }

}