package com.iboot.pf.module.mes.service.qcitem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.*;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
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
 * {@link QcItemServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(QcItemServiceImpl.class)
public class QcItemServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcItemServiceImpl qcItemService;

    @Resource
    private QcItemMapper qcItemMapper;

    @Test
    public void testCreateQcItem_success() {
        // 准备参数
        QcItemSaveReqVO createReqVO = randomPojo(QcItemSaveReqVO.class).setId(null);

        // 调用
        Long qcItemId = qcItemService.createQcItem(createReqVO);
        // 断言
        assertNotNull(qcItemId);
        // 校验记录的属性是否正确
        QcItemDO qcItem = qcItemMapper.selectById(qcItemId);
        assertPojoEquals(createReqVO, qcItem, "id");
    }

    @Test
    public void testUpdateQcItem_success() {
        // mock 数据
        QcItemDO dbQcItem = randomPojo(QcItemDO.class);
        qcItemMapper.insert(dbQcItem);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QcItemSaveReqVO updateReqVO = randomPojo(QcItemSaveReqVO.class, o -> {
            o.setId(dbQcItem.getId()); // 设置更新的 ID
        });

        // 调用
        qcItemService.updateQcItem(updateReqVO);
        // 校验是否更新正确
        QcItemDO qcItem = qcItemMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, qcItem);
    }

    @Test
    public void testUpdateQcItem_notExists() {
        // 准备参数
        QcItemSaveReqVO updateReqVO = randomPojo(QcItemSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> qcItemService.updateQcItem(updateReqVO), QC_ITEM_NOT_EXISTS);
    }

    @Test
    public void testDeleteQcItem_success() {
        // mock 数据
        QcItemDO dbQcItem = randomPojo(QcItemDO.class);
        qcItemMapper.insert(dbQcItem);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQcItem.getId();

        // 调用
        qcItemService.deleteQcItem(id);
       // 校验数据不存在了
       assertNull(qcItemMapper.selectById(id));
    }

    @Test
    public void testDeleteQcItem_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> qcItemService.deleteQcItem(id), QC_ITEM_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQcItemPage() {
       // mock 数据
       QcItemDO dbQcItem = randomPojo(QcItemDO.class, o -> { // 等会查询到
           o.setPlanId(null);
           o.setItemCode(null);
           o.setItemName(null);
           o.setItemType(null);
           o.setUnit(null);
           o.setStdValue(null);
           o.setUpperLimit(null);
           o.setLowerLimit(null);
           o.setRequiredFlag(null);
           o.setSortNo(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       qcItemMapper.insert(dbQcItem);
       // 测试 planId 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setPlanId(null)));
       // 测试 itemCode 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setItemCode(null)));
       // 测试 itemName 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setItemName(null)));
       // 测试 itemType 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setItemType(null)));
       // 测试 unit 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setUnit(null)));
       // 测试 stdValue 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setStdValue(null)));
       // 测试 upperLimit 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setUpperLimit(null)));
       // 测试 lowerLimit 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setLowerLimit(null)));
       // 测试 requiredFlag 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setRequiredFlag(null)));
       // 测试 sortNo 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setSortNo(null)));
       // 测试 status 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       qcItemMapper.insert(cloneIgnoreId(dbQcItem, o -> o.setCreateTime(null)));
       // 准备参数
       QcItemPageReqVO reqVO = new QcItemPageReqVO();
       reqVO.setPlanId(null);
       reqVO.setItemCode(null);
       reqVO.setItemName(null);
       reqVO.setItemType(null);
       reqVO.setUnit(null);
       reqVO.setStdValue(null);
       reqVO.setUpperLimit(null);
       reqVO.setLowerLimit(null);
       reqVO.setRequiredFlag(null);
       reqVO.setSortNo(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QcItemRespVO> pageResult = qcItemService.getQcItemPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQcItem, pageResult.getList().get(0));
    }

}