package com.iboot.pf.module.wms.service.inordermgr;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.InOrderLineSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderLineMapper;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.service.snapshot.WmsSnapshotService;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import java.math.BigDecimal;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.IN_ORDER_LINE_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 入库单 Service 测试：覆盖单号自动分配、单号唯一、存在性校验、子表行号唯一与快照补全、主单删除级联。
 */
@Import(InOrderServiceImpl.class)
class InOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InOrderService inOrderService;
    @Resource
    private WmsInOrderMapper inOrderMapper;
    @Resource
    private WmsInOrderLineMapper inOrderLineMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private WmsSnapshotService snapshotService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "IN0001" : invocation.getArgument(1));
        // 物料与单位快照来自 mdm 门面，单测只验证字段被写入
        MaterialRespDTO material = new MaterialRespDTO();
        material.setCode("M-001");
        material.setName("物料A");
        when(snapshotService.requireMaterial(any())).thenReturn(material);
        when(snapshotService.requireUomCode(any())).thenReturn("PCS");
    }

    private InOrderSaveReqVO buildReqVO(String inNo) {
        InOrderSaveReqVO reqVO = new InOrderSaveReqVO();
        reqVO.setInNo(inNo);
        reqVO.setBizType(WmsInBizTypeEnum.PO_IN.name());
        reqVO.setInvOrgId(1L);
        reqVO.setPlantId(1L);
        reqVO.setProjectId(0L);
        reqVO.setProjectTaskId(0L);
        reqVO.setWhId(1L);
        reqVO.setWhCode("WH-1");
        reqVO.setFromInvOrgId(0L);
        reqVO.setFromWhId(0L);
        reqVO.setNeedQc(false);
        reqVO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setHookMode(WmsHookModeEnum.NONE.name());
        reqVO.setStatus(WmsOrderStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateInOrder_blankNoFilledByRule() {
        Long id = inOrderService.createInOrder(buildReqVO(null));

        WmsInOrderDO entity = inOrderMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("IN0001", entity.getInNo());
    }

    @Test
    void testCreateInOrder_duplicateNoRejected() {
        inOrderService.createInOrder(buildReqVO("IN0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> inOrderService.createInOrder(buildReqVO("IN0-A")), IN_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateInOrder_keepOwnNo() {
        Long id = inOrderService.createInOrder(buildReqVO("IN0-A"));

        InOrderSaveReqVO updateReqVO = buildReqVO("IN0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        inOrderService.updateInOrder(updateReqVO);

        assertEquals("单据备注调整", inOrderMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateInOrder_duplicateNoRejected() {
        inOrderService.createInOrder(buildReqVO("IN0-A"));
        Long secondId = inOrderService.createInOrder(buildReqVO("IN0-B"));

        InOrderSaveReqVO updateReqVO = buildReqVO("IN0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> inOrderService.updateInOrder(updateReqVO), IN_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateInOrder_notExists() {
        InOrderSaveReqVO updateReqVO = buildReqVO("IN0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> inOrderService.updateInOrder(updateReqVO), IN_ORDER_NOT_EXISTS);
    }

    @Test
    void testDeleteInOrder_notExists() {
        assertServiceException(() -> inOrderService.deleteInOrder(randomLongId()), IN_ORDER_NOT_EXISTS);
    }

    private InOrderLineSaveReqVO buildLineReqVO(Long parentId, Integer lineNo) {
        InOrderLineSaveReqVO reqVO = new InOrderLineSaveReqVO();
        reqVO.setOrderId(parentId);
        reqVO.setLineNo(lineNo);
        reqVO.setMatId(1L);
        reqVO.setUomId(1L);
        reqVO.setLocId(1L);
        reqVO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setPlanQty(new BigDecimal("100"));
        reqVO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateInOrderLine_duplicateLineNoRejected() {
        Long parentId = inOrderService.createInOrder(buildReqVO("IN0-A"));
        inOrderService.createInOrderLine(buildLineReqVO(parentId, 10));

        // 同一单据下行号唯一，唯一索引对应的前置校验
        assertServiceException(() -> inOrderService.createInOrderLine(buildLineReqVO(parentId, 10)),
                IN_ORDER_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateInOrderLine_snapshotFilled() {
        Long parentId = inOrderService.createInOrder(buildReqVO("IN0-A"));
        Long lineId = inOrderService.createInOrderLine(buildLineReqVO(parentId, 10));

        // 物料与单位快照写入时落库，列表查询不再回查 mdm
        WmsInOrderLineDO line = inOrderLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("M-001", line.getMatCode());
        assertEquals("物料A", line.getMatName());
        assertEquals("PCS", line.getUomCode());
    }

    @Test
    void testDeleteInOrder_cascadeLines() {
        Long parentId = inOrderService.createInOrder(buildReqVO("IN0-A"));
        inOrderService.createInOrderLine(buildLineReqVO(parentId, 10));

        inOrderService.deleteInOrder(parentId);

        // 主单删除必须级联清理明细，否则留下孤儿数据
        assertNull(inOrderMapper.selectById(parentId));
        assertEquals(0, inOrderLineMapper.selectCount("order_id", parentId));
    }

}
