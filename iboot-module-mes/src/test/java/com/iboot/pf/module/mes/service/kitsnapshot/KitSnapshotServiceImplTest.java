package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotPageReqVO;
import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.KitSnapshotRespVO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 齐套检查快照 Service 测试。
 *
 * <p>快照是齐套检查过账写入的账表，只提供查询与导出，所以这里只覆盖查询与模块内 JOIN 出的可读字段。
 */
@Import(KitSnapshotServiceImpl.class)
class KitSnapshotServiceImplTest extends BaseDbUnitTest {

    @Resource
    private KitSnapshotService kitSnapshotService;
    @Resource
    private KitSnapshotMapper kitSnapshotMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;

    @MockitoBean
    private MesRelationFiller relationFiller;

    /** 造一条齐套快照，附带一张生产工单用于验证 JOIN 出的单号。 */
    private KitSnapshotDO insertSnapshot(String checkNo) {
        ProdOrderDO prodOrder = new ProdOrderDO().setOrderNo("PO-0001");
        prodOrderMapper.insert(prodOrder);
        KitSnapshotDO snapshot = new KitSnapshotDO()
                .setCheckNo(checkNo)
                .setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name())
                .setProdOrderId(prodOrder.getId())
                .setWorkOrderId(MesConstants.NONE_ID)
                .setOpTaskId(MesConstants.NONE_ID)
                .setLineNo(MesConstants.FIRST_LINE_NO)
                .setMatId(1L)
                .setMatCode("MAT-001")
                .setMatName("定子铁芯")
                .setReqQty(new BigDecimal("100"))
                .setAvailQty(new BigDecimal("60"))
                .setReservedQty(BigDecimal.ZERO)
                .setIssuedQty(BigDecimal.ZERO)
                .setShortQty(new BigDecimal("40"))
                .setAltMatId(MesConstants.NONE_ID)
                .setResult(MesKitStatusEnum.SHORTAGE.name())
                .setCheckTime(LocalDateTime.now());
        kitSnapshotMapper.insert(snapshot);
        return snapshot;
    }

    @Test
    void testGetKitSnapshot_joinProdOrderNo() {
        KitSnapshotDO snapshot = insertSnapshot("KC-0001");

        KitSnapshotRespVO respVO = kitSnapshotService.getKitSnapshot(snapshot.getId());

        assertNotNull(respVO);
        assertEquals("KC-0001", respVO.getCheckNo());
        // 生产工单号由模块内 JOIN 出来，列表不显示裸 ID
        assertEquals("PO-0001", respVO.getProdOrderNo());
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), respVO.getResult());
    }

    @Test
    void testGetKitSnapshot_notExists() {
        assertNull(kitSnapshotService.getKitSnapshot(999L));
    }

    @Test
    void testGetKitSnapshotPage_filterByResult() {
        insertSnapshot("KC-0001");

        KitSnapshotPageReqVO reqVO = new KitSnapshotPageReqVO();
        reqVO.setResult(MesKitStatusEnum.SHORTAGE.name());
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<KitSnapshotRespVO> list = kitSnapshotService.getKitSnapshotPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("KC-0001", list.get(0).getCheckNo());
    }

    @Test
    void testGetKitSnapshotPage_resultNotMatch() {
        insertSnapshot("KC-0001");

        KitSnapshotPageReqVO reqVO = new KitSnapshotPageReqVO();
        reqVO.setResult(MesKitStatusEnum.FULL_KIT.name());
        reqVO.setPageSize(PAGE_SIZE_NONE);

        // 不分页查询不会执行 count，所以断言列表长度而不是 total
        assertEquals(0, kitSnapshotService.getKitSnapshotPage(reqVO).getList().size());
    }

}
