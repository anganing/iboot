package com.iboot.pf.module.mes.service.proddemand;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandPageReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesImportResolver;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_HAS_PROD_ORDER;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 生产需求 Service 测试：编号自动分配、物料与单位快照落库、终态禁改、已生成工单禁删、幂等键唯一。
 */
@Import(ProdDemandServiceImpl.class)
class ProdDemandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdDemandService prodDemandService;
    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;
    @MockitoBean
    private MesImportResolver importResolver;

    @BeforeEach
    void mockExternalDependencies() {
        // 编号由 infra 编码规则分配：调用方填了就沿用，没填才走规则
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "DMD0001" : invocation.getArgument(1));
        // 物料与单位是 mdm 的数据，通过门面获取
        when(snapshotService.requireMaterial(any())).thenReturn(MesTestFixtures.material());
        when(snapshotService.requireUomCode(any())).thenReturn(MesTestFixtures.uomCode());
    }

    private ProdDemandSaveReqVO buildReqVO(String demandNo) {
        ProdDemandSaveReqVO reqVO = new ProdDemandSaveReqVO();
        reqVO.setDemandNo(demandNo);
        reqVO.setSourceType(MesDemandSourceTypeEnum.MANUAL.name());
        reqVO.setSourceNo("SO-0001");
        reqVO.setPlantId(1L);
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(1L);
        reqVO.setQty(new BigDecimal("100"));
        reqVO.setUomId(1L);
        reqVO.setDueDate(LocalDateTime.now().plusDays(7));
        reqVO.setPriority(0);
        reqVO.setStatus(MesDemandStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateProdDemand_blankNoFilledByRuleAndSnapshotFilled() {
        Long id = prodDemandService.createProdDemand(buildReqVO(null));

        ProdDemandDO demand = prodDemandMapper.selectById(id);
        assertNotNull(demand);
        assertEquals("DMD0001", demand.getDemandNo());
        // 物料与单位快照由后端补全，前端只提交编号
        assertEquals("MAT-001", demand.getMatCode());
        assertEquals("定子铁芯", demand.getMatName());
        assertEquals("PCS", demand.getUomCode());
    }

    @Test
    void testCreateProdDemand_duplicateIdempotencyKeyRejected() {
        ProdDemandSaveReqVO first = buildReqVO("DMD-A");
        first.setIdempotencyKey("ERP:SO-0001");
        prodDemandService.createProdDemand(first);

        ProdDemandSaveReqVO second = buildReqVO("DMD-B");
        second.setIdempotencyKey("ERP:SO-0001");
        assertServiceException(() -> prodDemandService.createProdDemand(second), IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Test
    void testUpdateProdDemand_keepDemandNo() {
        Long id = prodDemandService.createProdDemand(buildReqVO("DMD-A"));

        ProdDemandSaveReqVO updateReqVO = buildReqVO("DMD-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("交期调整");
        prodDemandService.updateProdDemand(updateReqVO);

        ProdDemandDO demand = prodDemandMapper.selectById(id);
        // 编号由编码规则分配，更新时不允许被表单改掉
        assertEquals("DMD-A", demand.getDemandNo());
        assertEquals("交期调整", demand.getRemark());
    }

    @Test
    void testUpdateProdDemand_finalStatusRejected() {
        ProdDemandSaveReqVO createReqVO = buildReqVO("DMD-A");
        createReqVO.setStatus(MesDemandStatusEnum.DONE.name());
        Long id = prodDemandService.createProdDemand(createReqVO);

        ProdDemandSaveReqVO updateReqVO = buildReqVO("DMD-A");
        updateReqVO.setId(id);
        assertServiceException(() -> prodDemandService.updateProdDemand(updateReqVO), PROD_DEMAND_STATUS_INVALID);
    }

    @Test
    void testUpdateProdDemand_notExists() {
        ProdDemandSaveReqVO updateReqVO = buildReqVO("DMD-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> prodDemandService.updateProdDemand(updateReqVO), PROD_DEMAND_NOT_EXISTS);
    }

    @Test
    void testDeleteProdDemand_success() {
        Long id = prodDemandService.createProdDemand(buildReqVO("DMD-A"));

        prodDemandService.deleteProdDemand(id);

        assertNull(prodDemandMapper.selectById(id));
    }

    @Test
    void testDeleteProdDemand_hasProdOrderRejected() {
        Long id = prodDemandService.createProdDemand(buildReqVO("DMD-A"));
        prodOrderMapper.insert(new ProdOrderDO().setDemandId(id).setOrderNo("PO-0001"));

        assertServiceException(() -> prodDemandService.deleteProdDemand(id), PROD_DEMAND_HAS_PROD_ORDER);
    }

    @Test
    void testDeleteProdDemandList_hasProdOrderRejected() {
        Long first = prodDemandService.createProdDemand(buildReqVO("DMD-A"));
        Long second = prodDemandService.createProdDemand(buildReqVO("DMD-B"));
        prodOrderMapper.insert(new ProdOrderDO().setDemandId(second).setOrderNo("PO-0001"));

        // 批量删除同样逐条校验占用，否则会留下引用不到需求的工单
        assertServiceException(() -> prodDemandService.deleteProdDemandListByIds(List.of(first, second)),
                PROD_DEMAND_HAS_PROD_ORDER);
        assertNotNull(prodDemandMapper.selectById(first));
    }

    @Test
    void testGetProdDemandPage() {
        prodDemandService.createProdDemand(buildReqVO("DMD-A"));

        ProdDemandPageReqVO reqVO = new ProdDemandPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<ProdDemandRespVO> list = prodDemandService.getProdDemandPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("DMD-A", list.get(0).getDemandNo());
        assertEquals("MAT-001", list.get(0).getMatCode());
    }

}
