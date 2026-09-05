package com.iboot.pf.module.mes.api.demand;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseMockitoUnitTest;
import com.iboot.pf.module.mes.api.demand.dto.CreateProdDemandReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandActionReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandRespDTO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.service.proddemand.MesProdDemandDomainService;
import com.iboot.pf.module.mes.service.proddemand.ProdDemandService;
import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 生产需求门面委托测试。
 *
 * <p>盯三处：建单要补上门面 DTO 没有而 codegen VO 要求非空的状态与两个项目外键、
 * 幂等键要原样传到 codegen（{@code mes_prod_demand.idempotency_key} 靠它判重）、
 * 取消的 {@code reason} 要落到 BO 的 {@code remark}（它是"这批需求为什么没投产"的唯一线索）。
 */
class ProdDemandApiImplTest extends BaseMockitoUnitTest {

    private static final String IDEMPOTENCY_KEY = "UI:PD-0001:CREATE_DEMAND:0";
    private static final String SOURCE_MODULE = "INTEGRATION";
    private static final String SOURCE_BIZ_NO = "SO-2026-0001";

    @InjectMocks
    private ProdDemandApiImpl prodDemandApi;

    @Mock
    private ProdDemandService prodDemandService;
    @Mock
    private MesProdDemandDomainService prodDemandDomainService;

    @Test
    void testCreateProdDemand_delegatesToCodegenWithFullMapping() {
        CreateProdDemandReqDTO reqDTO = new CreateProdDemandReqDTO();
        reqDTO.setIdempotencyKey(IDEMPOTENCY_KEY);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setSourceType(MesDemandSourceTypeEnum.ERP.name());
        reqDTO.setSourceNo("SO-2026-0001");
        reqDTO.setPlantId(11L);
        reqDTO.setProjectId(31L);
        reqDTO.setProjectTaskId(41L);
        reqDTO.setMatId(51L);
        reqDTO.setQty(new BigDecimal("100"));
        reqDTO.setUomId(61L);
        reqDTO.setDueDate(LocalDateTime.of(2026, 10, 1, 8, 0));
        reqDTO.setPriority(7);
        reqDTO.setRemark("销售订单下发");
        when(prodDemandService.createProdDemand(any())).thenReturn(2234L);

        assertEquals(2234L, prodDemandApi.createProdDemand(reqDTO));

        ArgumentCaptor<ProdDemandSaveReqVO> captor = ArgumentCaptor.forClass(ProdDemandSaveReqVO.class);
        verify(prodDemandService).createProdDemand(captor.capture());
        ProdDemandSaveReqVO saveReqVO = captor.getValue();
        assertNull(saveReqVO.getId());
        assertNull(saveReqVO.getDemandNo(), "需求单号必须留给编码规则生成");
        assertEquals(MesDemandSourceTypeEnum.ERP.name(), saveReqVO.getSourceType());
        assertEquals("SO-2026-0001", saveReqVO.getSourceNo());
        assertEquals(11L, saveReqVO.getPlantId());
        assertEquals(31L, saveReqVO.getProjectId());
        assertEquals(41L, saveReqVO.getProjectTaskId());
        assertEquals(51L, saveReqVO.getMatId());
        assertEquals(0, new BigDecimal("100").compareTo(saveReqVO.getQty()));
        assertEquals(61L, saveReqVO.getUomId());
        assertEquals(LocalDateTime.of(2026, 10, 1, 8, 0), saveReqVO.getDueDate());
        assertEquals(7, saveReqVO.getPriority());
        assertEquals("销售订单下发", saveReqVO.getRemark());
        // 新建需求固定落 DRAFT，状态不由调用方给
        assertEquals(MesDemandStatusEnum.DRAFT.name(), saveReqVO.getStatus());
        // 幂等键落到 VO 上，codegen 侧靠它做列判重；丢了就等于同一张需求可以被建两次
        assertEquals(IDEMPOTENCY_KEY, saveReqVO.getIdempotencyKey());
    }

    @Test
    void testCreateProdDemand_nullableIdsFallBackToNone() {
        CreateProdDemandReqDTO reqDTO = new CreateProdDemandReqDTO();
        reqDTO.setIdempotencyKey(IDEMPOTENCY_KEY);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setSourceType(MesDemandSourceTypeEnum.MANUAL.name());
        reqDTO.setPlantId(11L);
        reqDTO.setMatId(51L);
        reqDTO.setQty(new BigDecimal("100"));
        reqDTO.setUomId(61L);
        reqDTO.setDueDate(LocalDateTime.of(2026, 10, 1, 8, 0));

        prodDemandApi.createProdDemand(reqDTO);

        ArgumentCaptor<ProdDemandSaveReqVO> captor = ArgumentCaptor.forClass(ProdDemandSaveReqVO.class);
        verify(prodDemandService).createProdDemand(captor.capture());
        assertEquals(MesConstants.NONE_ID, captor.getValue().getProjectId());
        assertEquals(MesConstants.NONE_ID, captor.getValue().getProjectTaskId());
        assertEquals(0, captor.getValue().getPriority());
    }

    @Test
    void testCancelProdDemand_delegatesWithReasonMappedToRemark() {
        ProdDemandActionReqDTO reqDTO = new ProdDemandActionReqDTO();
        reqDTO.setIdempotencyKey("UI:PD-0001:CANCEL_DEMAND:0");
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setDemandId(88L);
        reqDTO.setReason("客户撤单");

        prodDemandApi.cancelProdDemand(reqDTO);

        ArgumentCaptor<MesConfirmDemandBO> captor = ArgumentCaptor.forClass(MesConfirmDemandBO.class);
        verify(prodDemandDomainService).cancelDemand(captor.capture());
        MesConfirmDemandBO bo = captor.getValue();
        assertEquals(88L, bo.getDemandId());
        assertEquals("客户撤单", bo.getRemark());
        assertEquals("UI:PD-0001:CANCEL_DEMAND:0", bo.getIdempotencyKey());
        assertEquals(SOURCE_MODULE, bo.getSourceModule());
        assertEquals(SOURCE_BIZ_NO, bo.getSourceBizNo());
    }

    @Test
    void testGetProdDemand_mapsSnapshotFields() {
        ProdDemandDO demand = new ProdDemandDO();
        demand.setId(88L).setDemandNo("PD-0001")
                .setSourceType(MesDemandSourceTypeEnum.ERP.name()).setSourceNo("SO-2026-0001")
                .setPlantId(11L).setProjectId(31L).setProjectTaskId(41L)
                .setMatId(51L).setMatCode("MAT-1001").setMatName("物料1001").setMatSpec("规格A")
                .setQty(new BigDecimal("100")).setUomId(61L).setUomCode("PCS")
                .setDueDate(LocalDateTime.of(2026, 10, 1, 8, 0)).setPriority(7)
                .setStatus(MesDemandStatusEnum.CONFIRMED.name());
        when(prodDemandService.getProdDemandList(List.of(88L))).thenReturn(List.of(demand));

        ProdDemandRespDTO respDTO = prodDemandApi.getProdDemand(88L);

        assertNotNull(respDTO);
        assertEquals("PD-0001", respDTO.getDemandNo());
        assertEquals("MAT-1001", respDTO.getMatCode());
        assertEquals("物料1001", respDTO.getMatName());
        assertEquals("规格A", respDTO.getMatSpec());
        assertEquals("PCS", respDTO.getUomCode());
        assertEquals(MesDemandStatusEnum.CONFIRMED.name(), respDTO.getStatus());
        assertEquals(MesDemandSourceTypeEnum.ERP.name(), respDTO.getSourceType());
        assertEquals(0, new BigDecimal("100").compareTo(respDTO.getQty()));
    }

    @Test
    void testGetProdDemand_nullIdAndEmptyIds() {
        assertNull(prodDemandApi.getProdDemand(null));
        assertTrue(prodDemandApi.getProdDemandList(List.of()).isEmpty());
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = ProdDemandApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : ProdDemandApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

}
