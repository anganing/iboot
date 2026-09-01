package com.iboot.pf.module.mes.service.wiptxn;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnPageReqVO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 在制流转流水 Service 测试。
 *
 * <p>流水是过账留痕的证据链，只提供查询与导出，所以这里只覆盖查询与模块内 JOIN 出的可读字段。
 */
@Import(WipTxnServiceImpl.class)
class WipTxnServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WipTxnService wipTxnService;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    /** 造一条流水，附带工序任务用于验证 JOIN 出的任务号与工序名。 */
    private WipTxnDO insertTxn(String txnNo, String txnType) {
        OpTaskDO opTask = new OpTaskDO()
                .setTaskNo("OPT-0001")
                .setWorkOrderId(1L)
                .setRouteOpId(31L)
                .setOpName("定子绕线");
        opTaskMapper.insert(opTask);
        WipTxnDO txn = new WipTxnDO()
                .setTxnNo(txnNo)
                .setProdOrderId(1L)
                .setWorkOrderId(1L)
                .setOpTaskId(opTask.getId())
                .setTxnType(txnType)
                .setQty(new BigDecimal("10"))
                .setFromStatus(MesOpTaskStatusEnum.READY.name())
                .setToStatus(MesOpTaskStatusEnum.RUNNING.name())
                .setBizType(MesBizTypeEnum.OP_TASK.name())
                .setBizNo("OPT-0001")
                .setFromOpTaskId(MesConstants.NONE_ID)
                .setToOpTaskId(MesConstants.NONE_ID)
                .setTxnTime(LocalDateTime.now());
        wipTxnMapper.insert(txn);
        return txn;
    }

    @Test
    void testGetWipTxn_joinOpTaskInfo() {
        WipTxnDO txn = insertTxn("WT-0001", MesWipTxnTypeEnum.START.name());

        WipTxnRespVO respVO = wipTxnService.getWipTxn(txn.getId());

        assertNotNull(respVO);
        assertEquals("WT-0001", respVO.getTxnNo());
        // 工序任务号与工序名由模块内 JOIN 出来
        assertEquals("OPT-0001", respVO.getOpTaskNo());
        assertEquals("定子绕线", respVO.getOpName());
    }

    @Test
    void testGetWipTxn_notExists() {
        assertNull(wipTxnService.getWipTxn(999L));
    }

    @Test
    void testGetWipTxnPage_filterByTxnType() {
        insertTxn("WT-0001", MesWipTxnTypeEnum.START.name());
        insertTxn("WT-0002", MesWipTxnTypeEnum.COMPLETE.name());

        WipTxnPageReqVO reqVO = new WipTxnPageReqVO();
        reqVO.setTxnType(MesWipTxnTypeEnum.START.name());
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<WipTxnRespVO> list = wipTxnService.getWipTxnPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("WT-0001", list.get(0).getTxnNo());
    }

    @Test
    void testGetWipTxnPage_txnNoLikeMatch() {
        insertTxn("WT-0001", MesWipTxnTypeEnum.START.name());

        WipTxnPageReqVO reqVO = new WipTxnPageReqVO();
        // 单号类字段走 LIKE，方便现场按片段搜
        reqVO.setTxnNo("0001");
        reqVO.setPageSize(PAGE_SIZE_NONE);

        // 不分页查询不会执行 count，所以断言列表长度而不是 total
        assertEquals(1, wipTxnService.getWipTxnPage(reqVO).getList().size());
    }

}
