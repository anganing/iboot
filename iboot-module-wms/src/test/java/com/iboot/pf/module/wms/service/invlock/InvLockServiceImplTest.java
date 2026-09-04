package com.iboot.pf.module.wms.service.invlock;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.wms.controller.admin.invlock.vo.InvLockSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.lock.WmsInvLockDO;
import com.iboot.pf.module.wms.dal.mysql.lock.WmsInvLockMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsLockStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import java.math.BigDecimal;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_LOCK_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_LOCK_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 库存冻结记录 Service 测试：覆盖单号自动分配、单号唯一、存在性校验。
 */
@Import(InvLockServiceImpl.class)
class InvLockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvLockService invLockService;
    @Resource
    private WmsInvLockMapper invLockMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private MaterialApi materialApi;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "LCK0001" : invocation.getArgument(1));
    }

    private InvLockSaveReqVO buildReqVO(String lockNo) {
        InvLockSaveReqVO reqVO = new InvLockSaveReqVO();
        reqVO.setLockNo(lockNo);
        reqVO.setBizType(WmsSourceBizTypeEnum.QC_HOLD.name());
        reqVO.setBizNo("QC-0001");
        reqVO.setInvOrgId(1L);
        reqVO.setWhId(1L);
        reqVO.setLocId(1L);
        reqVO.setMatId(1L);
        reqVO.setBatchId(1L);
        reqVO.setContainerId(0L);
        reqVO.setProjectId(0L);
        reqVO.setProjectTaskId(0L);
        reqVO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setQty(new BigDecimal("10"));
        reqVO.setReleasedQty(BigDecimal.ZERO);
        reqVO.setReason("质检待判");
        reqVO.setStatus(WmsLockStatusEnum.ACTIVE.name());
        reqVO.setIdempotencyKey("MES:QC-0001:FREEZE");
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateInvLock_blankNoFilledByRule() {
        Long id = invLockService.createInvLock(buildReqVO(null));

        WmsInvLockDO entity = invLockMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("LCK0001", entity.getLockNo());
    }

    @Test
    void testCreateInvLock_duplicateNoRejected() {
        invLockService.createInvLock(buildReqVO("LCK-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> invLockService.createInvLock(buildReqVO("LCK-A")), INV_LOCK_NO_DUPLICATE);
    }

    @Test
    void testUpdateInvLock_keepOwnNo() {
        Long id = invLockService.createInvLock(buildReqVO("LCK-A"));

        InvLockSaveReqVO updateReqVO = buildReqVO("LCK-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        invLockService.updateInvLock(updateReqVO);

        assertEquals("单据备注调整", invLockMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateInvLock_duplicateNoRejected() {
        invLockService.createInvLock(buildReqVO("LCK-A"));
        Long secondId = invLockService.createInvLock(buildReqVO("LCK-B"));

        InvLockSaveReqVO updateReqVO = buildReqVO("LCK-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> invLockService.updateInvLock(updateReqVO), INV_LOCK_NO_DUPLICATE);
    }

    @Test
    void testUpdateInvLock_notExists() {
        InvLockSaveReqVO updateReqVO = buildReqVO("LCK-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> invLockService.updateInvLock(updateReqVO), INV_LOCK_NOT_EXISTS);
    }

    @Test
    void testDeleteInvLock_notExists() {
        assertServiceException(() -> invLockService.deleteInvLock(randomLongId()), INV_LOCK_NOT_EXISTS);
    }

    @Test
    void testDeleteInvLock_success() {
        Long id = invLockService.createInvLock(buildReqVO("LCK-A"));

        invLockService.deleteInvLock(id);

        assertNull(invLockMapper.selectById(id));
    }

}
