package com.iboot.pf.module.wms.service.batchmgr;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.BatchSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.batchmgr.BatchDO;
import com.iboot.pf.module.wms.dal.mysql.batchmgr.BatchMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_LOT_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 批次 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(BatchServiceImpl.class)
class BatchServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BatchService batchService;
    @Resource
    private BatchMapper batchMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "LOT0001" : invocation.getArgument(1));
    }

    private BatchSaveReqVO buildReqVO(String lotNo) {
        BatchSaveReqVO reqVO = new BatchSaveReqVO();
        reqVO.setMatId(1L);
        reqVO.setMatCode("M-001");
        reqVO.setMatName("物料A");
        reqVO.setLotNo(lotNo);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateBatch_blankCodeFilledByRule() {
        Long id = batchService.createBatch(buildReqVO(null));

        BatchDO entity = batchMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("LOT0001", entity.getLotNo());
    }

    @Test
    void testCreateBatch_duplicateRejected() {
        batchService.createBatch(buildReqVO("LOT-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> batchService.createBatch(buildReqVO("LOT-A")), BATCH_LOT_NO_DUPLICATE);
    }

    @Test
    void testUpdateBatch_keepOwnCode() {
        Long id = batchService.createBatch(buildReqVO("LOT-A"));

        // 改自己时业务编码没变，不应该撞到自己
        BatchSaveReqVO updateReqVO = buildReqVO("LOT-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        batchService.updateBatch(updateReqVO);

        assertEquals("规则调整", batchMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateBatch_duplicateRejected() {
        batchService.createBatch(buildReqVO("LOT-A"));
        Long secondId = batchService.createBatch(buildReqVO("LOT-B"));

        BatchSaveReqVO updateReqVO = buildReqVO("LOT-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> batchService.updateBatch(updateReqVO), BATCH_LOT_NO_DUPLICATE);
    }

    @Test
    void testUpdateBatch_notExists() {
        BatchSaveReqVO updateReqVO = buildReqVO("LOT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> batchService.updateBatch(updateReqVO), BATCH_NOT_EXISTS);
    }

    @Test
    void testDeleteBatch_success() {
        Long id = batchService.createBatch(buildReqVO("LOT-A"));

        batchService.deleteBatch(id);

        assertNull(batchMapper.selectById(id));
    }

    @Test
    void testDeleteBatch_notExists() {
        assertServiceException(() -> batchService.deleteBatch(randomLongId()), BATCH_NOT_EXISTS);
    }

}
