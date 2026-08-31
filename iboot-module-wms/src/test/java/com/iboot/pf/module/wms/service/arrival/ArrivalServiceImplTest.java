package com.iboot.pf.module.wms.service.arrival;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.ArrivalSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.ArrivalLineSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;
import com.iboot.pf.module.wms.dal.mysql.arrivalline.ArrivalLineMapper;
import com.iboot.pf.module.wms.enums.WmsArrivalLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalSourceTypeEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalStatusEnum;
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
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_NO_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ARRIVAL_LINE_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 到货登记 Service 测试：覆盖单号自动分配、单号唯一、存在性校验、子表行号唯一与快照补全、主单删除级联。
 */
@Import(ArrivalServiceImpl.class)
class ArrivalServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ArrivalService arrivalService;
    @Resource
    private ArrivalMapper arrivalMapper;
    @Resource
    private ArrivalLineMapper arrivalLineMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private WmsSnapshotService snapshotService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "ARR0001" : invocation.getArgument(1));
        // 物料与单位快照来自 mdm 门面，单测只验证字段被写入
        MaterialRespDTO material = new MaterialRespDTO();
        material.setCode("M-001");
        material.setName("物料A");
        when(snapshotService.requireMaterial(any())).thenReturn(material);
        when(snapshotService.requireUomCode(any())).thenReturn("PCS");
    }

    private ArrivalSaveReqVO buildReqVO(String arrivalNo) {
        ArrivalSaveReqVO reqVO = new ArrivalSaveReqVO();
        reqVO.setArrivalNo(arrivalNo);
        reqVO.setSourceType(WmsArrivalSourceTypeEnum.PURCHASE_ORDER.name());
        reqVO.setSourceNo("PO-0001");
        reqVO.setInvOrgId(1L);
        reqVO.setPlantId(1L);
        reqVO.setWhId(1L);
        reqVO.setStatus(WmsArrivalStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateArrival_blankNoFilledByRule() {
        Long id = arrivalService.createArrival(buildReqVO(null));

        ArrivalDO entity = arrivalMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("ARR0001", entity.getArrivalNo());
    }

    @Test
    void testCreateArrival_duplicateNoRejected() {
        arrivalService.createArrival(buildReqVO("ARR-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> arrivalService.createArrival(buildReqVO("ARR-A")), ARRIVAL_NO_DUPLICATE);
    }

    @Test
    void testUpdateArrival_keepOwnNo() {
        Long id = arrivalService.createArrival(buildReqVO("ARR-A"));

        ArrivalSaveReqVO updateReqVO = buildReqVO("ARR-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        arrivalService.updateArrival(updateReqVO);

        assertEquals("单据备注调整", arrivalMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateArrival_duplicateNoRejected() {
        arrivalService.createArrival(buildReqVO("ARR-A"));
        Long secondId = arrivalService.createArrival(buildReqVO("ARR-B"));

        ArrivalSaveReqVO updateReqVO = buildReqVO("ARR-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> arrivalService.updateArrival(updateReqVO), ARRIVAL_NO_DUPLICATE);
    }

    @Test
    void testUpdateArrival_notExists() {
        ArrivalSaveReqVO updateReqVO = buildReqVO("ARR-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> arrivalService.updateArrival(updateReqVO), ARRIVAL_NOT_EXISTS);
    }

    @Test
    void testDeleteArrival_notExists() {
        assertServiceException(() -> arrivalService.deleteArrival(randomLongId()), ARRIVAL_NOT_EXISTS);
    }

    private ArrivalLineSaveReqVO buildLineReqVO(Long parentId, Integer lineNo) {
        ArrivalLineSaveReqVO reqVO = new ArrivalLineSaveReqVO();
        reqVO.setArrivalId(parentId);
        reqVO.setLineNo(lineNo);
        reqVO.setMatId(1L);
        reqVO.setArrivalQty(new BigDecimal("100"));
        reqVO.setUomId(1L);
        reqVO.setNeedQc(false);
        reqVO.setStatus(WmsArrivalLineStatusEnum.PENDING.name());
        reqVO.setRemark("单测明细");
        return reqVO;
    }

    @Test
    void testCreateArrivalLine_duplicateLineNoRejected() {
        Long parentId = arrivalService.createArrival(buildReqVO("ARR-A"));
        arrivalService.createArrivalLine(buildLineReqVO(parentId, 10));

        // 同一单据下行号唯一，唯一索引对应的前置校验
        assertServiceException(() -> arrivalService.createArrivalLine(buildLineReqVO(parentId, 10)),
                ARRIVAL_LINE_NO_DUPLICATE);
    }

    @Test
    void testCreateArrivalLine_snapshotFilled() {
        Long parentId = arrivalService.createArrival(buildReqVO("ARR-A"));
        Long lineId = arrivalService.createArrivalLine(buildLineReqVO(parentId, 10));

        // 物料与单位快照写入时落库，列表查询不再回查 mdm
        ArrivalLineDO line = arrivalLineMapper.selectById(lineId);
        assertNotNull(line);
        assertEquals("M-001", line.getMatCode());
        assertEquals("物料A", line.getMatName());
        assertEquals("PCS", line.getUomCode());
    }

    @Test
    void testDeleteArrival_cascadeLines() {
        Long parentId = arrivalService.createArrival(buildReqVO("ARR-A"));
        arrivalService.createArrivalLine(buildLineReqVO(parentId, 10));

        arrivalService.deleteArrival(parentId);

        // 主单删除必须级联清理明细，否则留下孤儿数据
        assertNull(arrivalMapper.selectById(parentId));
        assertEquals(0, arrivalLineMapper.selectCount("arrival_id", parentId));
    }

}
