package com.iboot.pf.module.wms.service.invreservation;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.InvReservationSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.invreservation.InvReservationDO;
import com.iboot.pf.module.wms.dal.mysql.invreservation.InvReservationMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;
import java.math.BigDecimal;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_RESERVATION_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_RESERVATION_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 库存预留 Service 测试：覆盖单号自动分配、单号唯一、存在性校验。
 */
@Import(InvReservationServiceImpl.class)
class InvReservationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvReservationService invReservationService;
    @Resource
    private InvReservationMapper invReservationMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "RSV0001" : invocation.getArgument(1));
    }

    private InvReservationSaveReqVO buildReqVO(String reservationNo) {
        InvReservationSaveReqVO reqVO = new InvReservationSaveReqVO();
        reqVO.setReservationNo(reservationNo);
        reqVO.setBizType(WmsSourceBizTypeEnum.PROD_RESERVE.name());
        reqVO.setBizNo("MO-0001");
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
        reqVO.setStatus(WmsReservationStatusEnum.ACTIVE.name());
        reqVO.setIdempotencyKey("MES:MO-0001:RESERVE");
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateInvReservation_blankNoFilledByRule() {
        Long id = invReservationService.createInvReservation(buildReqVO(null));

        InvReservationDO entity = invReservationMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("RSV0001", entity.getReservationNo());
    }

    @Test
    void testCreateInvReservation_duplicateNoRejected() {
        invReservationService.createInvReservation(buildReqVO("RSV-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> invReservationService.createInvReservation(buildReqVO("RSV-A")), INV_RESERVATION_NO_DUPLICATE);
    }

    @Test
    void testUpdateInvReservation_keepOwnNo() {
        Long id = invReservationService.createInvReservation(buildReqVO("RSV-A"));

        InvReservationSaveReqVO updateReqVO = buildReqVO("RSV-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        invReservationService.updateInvReservation(updateReqVO);

        assertEquals("单据备注调整", invReservationMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateInvReservation_duplicateNoRejected() {
        invReservationService.createInvReservation(buildReqVO("RSV-A"));
        Long secondId = invReservationService.createInvReservation(buildReqVO("RSV-B"));

        InvReservationSaveReqVO updateReqVO = buildReqVO("RSV-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> invReservationService.updateInvReservation(updateReqVO), INV_RESERVATION_NO_DUPLICATE);
    }

    @Test
    void testUpdateInvReservation_notExists() {
        InvReservationSaveReqVO updateReqVO = buildReqVO("RSV-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> invReservationService.updateInvReservation(updateReqVO), INV_RESERVATION_NOT_EXISTS);
    }

    @Test
    void testDeleteInvReservation_notExists() {
        assertServiceException(() -> invReservationService.deleteInvReservation(randomLongId()), INV_RESERVATION_NOT_EXISTS);
    }

    @Test
    void testDeleteInvReservation_success() {
        Long id = invReservationService.createInvReservation(buildReqVO("RSV-A"));

        invReservationService.deleteInvReservation(id);

        assertNull(invReservationMapper.selectById(id));
    }

}
