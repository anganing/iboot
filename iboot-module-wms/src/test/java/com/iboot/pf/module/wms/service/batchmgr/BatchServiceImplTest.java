package com.iboot.pf.module.wms.service.batchmgr;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.batchmgr.BatchDO;
import com.iboot.pf.module.wms.dal.mysql.batchmgr.BatchMapper;
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
 * {@link BatchServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(BatchServiceImpl.class)
public class BatchServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BatchServiceImpl batchService;

    @Resource
    private BatchMapper batchMapper;

    @Test
    public void testCreateBatch_success() {
        // 准备参数
        BatchSaveReqVO createReqVO = randomPojo(BatchSaveReqVO.class).setId(null);

        // 调用
        Long batchId = batchService.createBatch(createReqVO);
        // 断言
        assertNotNull(batchId);
        // 校验记录的属性是否正确
        BatchDO batch = batchMapper.selectById(batchId);
        assertPojoEquals(createReqVO, batch, "id");
    }

    @Test
    public void testUpdateBatch_success() {
        // mock 数据
        BatchDO dbBatch = randomPojo(BatchDO.class);
        batchMapper.insert(dbBatch);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BatchSaveReqVO updateReqVO = randomPojo(BatchSaveReqVO.class, o -> {
            o.setId(dbBatch.getId()); // 设置更新的 ID
        });

        // 调用
        batchService.updateBatch(updateReqVO);
        // 校验是否更新正确
        BatchDO batch = batchMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, batch);
    }

    @Test
    public void testUpdateBatch_notExists() {
        // 准备参数
        BatchSaveReqVO updateReqVO = randomPojo(BatchSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> batchService.updateBatch(updateReqVO), BATCH_NOT_EXISTS);
    }

    @Test
    public void testDeleteBatch_success() {
        // mock 数据
        BatchDO dbBatch = randomPojo(BatchDO.class);
        batchMapper.insert(dbBatch);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbBatch.getId();

        // 调用
        batchService.deleteBatch(id);
       // 校验数据不存在了
       assertNull(batchMapper.selectById(id));
    }

    @Test
    public void testDeleteBatch_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> batchService.deleteBatch(id), BATCH_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBatchPage() {
       // mock 数据
       BatchDO dbBatch = randomPojo(BatchDO.class, o -> { // 等会查询到
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setLotNo(null);
           o.setSnNo(null);
           o.setSupplierId(null);
           o.setSupplierCode(null);
           o.setSupplierLotNo(null);
           o.setFurnaceLotNo(null);
           o.setProduceDate(null);
           o.setExpiryDate(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       batchMapper.insert(dbBatch);
       // 测试 matId 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setMatName(null)));
       // 测试 lotNo 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setLotNo(null)));
       // 测试 snNo 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setSnNo(null)));
       // 测试 supplierId 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setSupplierId(null)));
       // 测试 supplierCode 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setSupplierCode(null)));
       // 测试 supplierLotNo 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setSupplierLotNo(null)));
       // 测试 furnaceLotNo 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setFurnaceLotNo(null)));
       // 测试 produceDate 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setProduceDate(null)));
       // 测试 expiryDate 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setExpiryDate(null)));
       // 测试 status 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       batchMapper.insert(cloneIgnoreId(dbBatch, o -> o.setCreateTime(null)));
       // 准备参数
       BatchPageReqVO reqVO = new BatchPageReqVO();
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setLotNo(null);
       reqVO.setSnNo(null);
       reqVO.setSupplierId(null);
       reqVO.setSupplierCode(null);
       reqVO.setSupplierLotNo(null);
       reqVO.setFurnaceLotNo(null);
       reqVO.setProduceDate(new java.time.LocalDate[]{java.time.LocalDate.of(2023, 2, 1), java.time.LocalDate.of(2023, 2, 28)});
       reqVO.setExpiryDate(new java.time.LocalDate[]{java.time.LocalDate.of(2023, 2, 1), java.time.LocalDate.of(2023, 2, 28)});
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<BatchRespVO> pageResult = batchService.getBatchPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBatch, pageResult.getList().get(0));
    }

}
