package com.iboot.pf.module.wms.service.batchmgr;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.BatchSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_LOT_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 批次 codegen Service 测试。
 *
 * <p>只剩 {@code updateBatch} 一个写入方法（批属性维护，见 package-info 的例外二），
 * 所以这里只覆盖它的三条路径：改自己不撞自己、撞别人的批次号被拦、批次不存在被拦。
 * 批次的创建能力已收归领域侧 {@code WmsBatchService#registerBatch}（由到货确认触发），
 * 对应覆盖在 {@code WmsArrivalOrderServiceImplTest} 的批次注册用例里。
 *
 * <p>批次由 Mapper 直接播种：codegen 侧的 {@code createBatch} 已删，
 * 领域侧的 {@code registerBatch} 又要拖进整条到货链路，播种是最短路径。
 */
@Import(BatchServiceImpl.class)
class BatchServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BatchService batchService;
    @Resource
    private WmsBatchMapper batchMapper;

    /** 播种一条批次主档，返回主键。 */
    private Long seedBatch(String lotNo) {
        WmsBatchDO batch = new WmsBatchDO();
        batch.setMatId(1L);
        batch.setMatCode("M-001");
        batch.setMatName("物料A");
        batch.setLotNo(lotNo);
        batch.setStatus(CommonStatusEnum.ENABLE.getStatus());
        batch.setRemark("单测数据");
        batchMapper.insert(batch);
        return batch.getId();
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
    void testUpdateBatch_keepOwnCode() {
        Long id = seedBatch("LOT-A");

        // 改自己时业务编码没变，不应该撞到自己
        BatchSaveReqVO updateReqVO = buildReqVO("LOT-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        batchService.updateBatch(updateReqVO);

        assertEquals("规则调整", batchMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateBatch_duplicateRejected() {
        seedBatch("LOT-A");
        Long secondId = seedBatch("LOT-B");

        // 同一物料下批次号唯一，改成别人的批次号必须被拦
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

}
