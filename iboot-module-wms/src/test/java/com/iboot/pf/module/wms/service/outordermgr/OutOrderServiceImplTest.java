package com.iboot.pf.module.wms.service.outordermgr;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.OutOrderSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.outordermgr.OutOrderDO;
import com.iboot.pf.module.wms.dal.mysql.outordermgr.OutOrderMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.OutOrderLineSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.outorderline.OutOrderLineDO;
import com.iboot.pf.module.wms.dal.mysql.outorderline.OutOrderLineMapper;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.OUT_ORDER_LINE_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 出库单 Service 测试：覆盖单号自动分配、单号唯一、存在性校验、子表行号唯一与快照补全、主单删除级联。
 */
@Import(OutOrderServiceImpl.class)
class OutOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutOrderService outOrderService;
    @Resource
    private OutOrderMapper outOrderMapper;
    @Resource
    private OutOrderLineMapper outOrderLineMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private WmsSnapshotService snapshotService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "OUT0001" : invocation.getArgument(1));
        // 物料与单位快照来自 mdm 门面，单测只验证字段被写入
        MaterialRespDTO material = new MaterialRespDTO();
        material.setCode("M-001");
        material.setName("物料A");
        when(snapshotService.requireMaterial(any())).thenReturn(material);
        when(snapshotService.requireUomCode(any())).thenReturn("PCS");
    }

    private OutOrderSaveReqVO buildReqVO(String outNo) {
        OutOrderSaveReqVO reqVO = new OutOrderSaveReqVO();
        reqVO.setOutNo(outNo);
        reqVO.setBizType(WmsOutBizTypeEnum.PROD_ISSUE_OUT.name());
        reqVO.setInvOrgId(1L);
        reqVO.setPlantId(1L);
        reqVO.setProjectId(0L);
        reqVO.setProjectTaskId(0L);
        reqVO.setWhId(1L);
        reqVO.setWhCode("WH-1");
        reqVO.setToInvOrgId(0L);
        reqVO.setToWhId(0L);
        reqVO.setHookMode(WmsHookModeEnum.NONE.name());
        reqVO.setStatus(WmsOrderStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateOutOrder_blankNoFilledByRule() {
        Long id = outOrderService.createOutOrder(buildReqVO(null));

        OutOrderDO entity = outOrderMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("OUT0001", entity.getOutNo());
    }

    @Test
    void testCreateOutOrder_duplicateNoRejected() {
        outOrderService.createOutOrder(buildReqVO("OUT-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> outOrderService.createOutOrder(buildReqVO("OUT-A")), OUT_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateOutOrder_keepOwnNo() {
        Long id = outOrderService.createOutOrder(buildReqVO("OUT-A"));

        OutOrderSaveReqVO updateReqVO = buildReqVO("OUT-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        outOrderService.updateOutOrder(updateReqVO);

        assertEquals("单据备注调整", outOrderMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateOutOrder_duplicateNoRejected() {
        outOrderService.createOutOrder(buildReqVO("OUT-A"));
        Long secondId = outOrderService.createOutOrder(buildReqVO("OUT-B"));

        OutOrderSaveReqVO updateReqVO = buildReqVO("OUT-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> outOrderService.updateOutOrder(updateReqVO), OUT_ORDER_NO_DUPLICATE);
    }

    @Test
    void testUpdateOutOrder_notExists() {
        OutOrderSaveReqVO updateReqVO = buildReqVO("OUT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> outOrderService.updateOutOrder(updateReqVO), OUT_ORDER_NOT_EXISTS);
    }

    @Test
    void testDeleteOutOrder_notExists() {
        assertServiceException(() -> outOrderService.deleteOutOrder(randomLongId()), OUT_ORDER_NOT_EXISTS);
    }

    private OutOrderLineSaveReqVO buildLineReqVO(Long parentId, Integer lineNo) {
        OutOrderLineSaveReqVO reqVO = new OutOrderLineSaveReqVO();
        reqVO.setOrderId(parentId);
        reqVO.setLineNo(lineNo);
        reqVO.setMatId(1L);
        reqVO.setUomId(1L);
        reqVO.setFromLocId(1L);
        reqVO.setQualityStatus(WmsQualityStatusEnum.QUALIFIED.name());
        reqVO.setPlanQty(new BigDecimal("100"));
        reqVO.setStatus(WmsOrderLineStatusEnum.DRAFT.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateOutOrderLine_duplicateLineNoRejected() {
        Long parentId = outOrderService.createOutOrder(buildReqVO("OUT-A"));
        outOrderService.createOutOrderLine(buildLineReqVO(parentId, 10));

        // 同一单据下行号唯一，唯一索引对应的前置校验
        assertServiceException(() -> outOrderService.createOutOrderLine(buildLineReqVO(parentId, 10)),
                OUT_ORDER_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateOutOrderLine_snapshotFilled() {
        Long parentId = outOrderService.createOutOrder(buildReqVO("OUT-A"));
        Long lineId = outOrderService.createOutOrderLine(buildLineReqVO(parentId, 10));

        // 物料与单位快照写入时落库，列表查询不再回查 mdm
        OutOrderLineDO line = outOrderLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("M-001", line.getMatCode());
        assertEquals("物料A", line.getMatName());
        assertEquals("PCS", line.getUomCode());
    }

    @Test
    void testDeleteOutOrder_cascadeLines() {
        Long parentId = outOrderService.createOutOrder(buildReqVO("OUT-A"));
        outOrderService.createOutOrderLine(buildLineReqVO(parentId, 10));

        outOrderService.deleteOutOrder(parentId);

        // 主单删除必须级联清理明细，否则留下孤儿数据
        assertNull(outOrderMapper.selectById(parentId));
        assertEquals(0, outOrderLineMapper.selectCount("order_id", parentId));
    }

}
