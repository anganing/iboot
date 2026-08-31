package com.iboot.pf.module.wms.service.transferorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.transferorder.TransferOrderDO;
import com.iboot.pf.module.wms.dal.mysql.transferorder.TransferOrderMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.controller.admin.transferorder.vo.TransferOrderLineSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.transferorderline.TransferOrderLineMapper;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsTransferTypeEnum;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.TRANSFER_ORDER_LINE_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 移库调拨单 Service 测试：覆盖单号自动分配、单号唯一、存在性校验、子表行号唯一与快照补全、主单删除级联。
 */
@Import(TransferOrderServiceImpl.class)
class TransferOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TransferOrderService transferOrderService;
    @Resource
    private TransferOrderMapper transferOrderMapper;
    @Resource
    private TransferOrderLineMapper transferOrderLineMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private WmsSnapshotService snapshotService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "TR0001" : invocation.getArgument(1));
        // 物料与单位快照来自 mdm 门面，单测只验证字段被写入
        MaterialRespDTO material = new MaterialRespDTO();
        material.setCode("M-001");
        material.setName("物料A");
        when(snapshotService.requireMaterial(any())).thenReturn(material);
        when(snapshotService.requireUomCode(any())).thenReturn("PCS");
    }

    private TransferOrderSaveReqVO buildReqVO(String transferNo) {
        TransferOrderSaveReqVO reqVO = new TransferOrderSaveReqVO();
        reqVO.setTransferNo(transferNo);
        reqVO.setTransferType(WmsTransferTypeEnum.LOC_TRANSFER.name());
        reqVO.setFromInvOrgId(1L);
        reqVO.setToInvOrgId(1L);
        reqVO.setFromWhId(1L);
        reqVO.setToWhId(2L);
        reqVO.setFromProjectId(0L);
        reqVO.setToProjectId(0L);
        reqVO.setHookMode(WmsHookModeEnum.NONE.name());
        reqVO.setStatus(WmsOrderStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateTransferOrder_blankNoFilledByRule() {
        Long id = transferOrderService.createTransferOrder(buildReqVO(null));

        TransferOrderDO entity = transferOrderMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("TR0001", entity.getTransferNo());
    }

    @Test
    void testCreateTransferOrder_duplicateNoRejected() {
        transferOrderService.createTransferOrder(buildReqVO("TR0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> transferOrderService.createTransferOrder(buildReqVO("TR0-A")), TRANSFER_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateTransferOrder_keepOwnNo() {
        Long id = transferOrderService.createTransferOrder(buildReqVO("TR0-A"));

        TransferOrderSaveReqVO updateReqVO = buildReqVO("TR0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        transferOrderService.updateTransferOrder(updateReqVO);

        assertEquals("单据备注调整", transferOrderMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateTransferOrder_duplicateNoRejected() {
        transferOrderService.createTransferOrder(buildReqVO("TR0-A"));
        Long secondId = transferOrderService.createTransferOrder(buildReqVO("TR0-B"));

        TransferOrderSaveReqVO updateReqVO = buildReqVO("TR0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> transferOrderService.updateTransferOrder(updateReqVO), TRANSFER_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateTransferOrder_notExists() {
        TransferOrderSaveReqVO updateReqVO = buildReqVO("TR0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> transferOrderService.updateTransferOrder(updateReqVO), TRANSFER_ORDER_NOT_EXISTS);
    }

    @Test
    void testDeleteTransferOrder_notExists() {
        assertServiceException(() -> transferOrderService.deleteTransferOrder(randomLongId()), TRANSFER_ORDER_NOT_EXISTS);
    }

    private TransferOrderLineSaveReqVO buildLineReqVO(Long parentId, Integer lineNo) {
        TransferOrderLineSaveReqVO reqVO = new TransferOrderLineSaveReqVO();
        reqVO.setOrderId(parentId);
        reqVO.setLineNo(lineNo);
        reqVO.setMatId(1L);
        reqVO.setUomId(1L);
        reqVO.setFromLocId(1L);
        reqVO.setToLocId(2L);
        reqVO.setQualityStatusFrom(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setQualityStatusTo(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setPlanQty(new BigDecimal("100"));
        reqVO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateTransferOrderLine_duplicateLineNoRejected() {
        Long parentId = transferOrderService.createTransferOrder(buildReqVO("TR0-A"));
        transferOrderService.createTransferOrderLine(buildLineReqVO(parentId, 10));

        // 同一单据下行号唯一，唯一索引对应的前置校验
        assertServiceException(() -> transferOrderService.createTransferOrderLine(buildLineReqVO(parentId, 10)),
                TRANSFER_ORDER_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateTransferOrderLine_snapshotFilled() {
        Long parentId = transferOrderService.createTransferOrder(buildReqVO("TR0-A"));
        Long lineId = transferOrderService.createTransferOrderLine(buildLineReqVO(parentId, 10));

        // 物料与单位快照写入时落库，列表查询不再回查 mdm
        TransferOrderLineDO line = transferOrderLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("M-001", line.getMatCode());
        assertEquals("物料A", line.getMatName());
        assertEquals("PCS", line.getUomCode());
    }

    @Test
    void testDeleteTransferOrder_cascadeLines() {
        Long parentId = transferOrderService.createTransferOrder(buildReqVO("TR0-A"));
        transferOrderService.createTransferOrderLine(buildLineReqVO(parentId, 10));

        transferOrderService.deleteTransferOrder(parentId);

        // 主单删除必须级联清理明细，否则留下孤儿数据
        assertNull(transferOrderMapper.selectById(parentId));
        assertEquals(0, transferOrderLineMapper.selectCount("order_id", parentId));
    }

}
