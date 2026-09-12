package com.iboot.pf.module.mes.service.trace;

import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExcelRowRespVO;
import com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 追溯链路树拍平成 Excel 行的单元测试（T5-03 导出）。
 *
 * <p>纯数据结构测试，不启 Spring 上下文、不碰解析器：拍平器只消费 {@link MesTraceNodeBO} 的
 * nodeType / bizNo / snapshot / children，因此手工搭一棵覆盖九类节点的树即可，
 * 断言三件导出必须成立的事：层级与路径列正确、枚举译成中文、快照不丢信息。
 */
public class MesTraceExcelFlattenerTest {

    private static MesTraceNodeBO node(MesTraceNodeTypeEnum type, String bizNo, String nodeName,
                                       int depth, Map<String, Object> snapshot, MesTraceNodeBO... children) {
        MesTraceNodeBO node = new MesTraceNodeBO()
                .setNodeType(type.name())
                .setBizNo(bizNo)
                .setNodeName(nodeName)
                .setDepth(depth)
                .setSnapshot(snapshot);
        List<MesTraceNodeBO> childList = new ArrayList<>();
        for (MesTraceNodeBO child : children) {
            childList.add(child);
        }
        return node.setChildren(childList);
    }

    private static Map<String, Object> snapshot(Object... keyValues) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            snapshot.put((String) keyValues[i], keyValues[i + 1]);
        }
        return snapshot;
    }

    /** 一条正向 + 反向合流的链路：原料批次 → 领料出库 → 工单 → 工序任务 → 完工入库 → 成品批次 → 销售出库 → 客户，工单下挂质检与在制，原料批次下挂供应商。 */
    private static MesTraceNodeBO fullChain() {
        MesTraceNodeBO supplier = node(MesTraceNodeTypeEnum.SUPPLIER, "SUP001", "宝钢国贸", 2,
                snapshot("supplierId", 1L, "supplierCode", "SUP001", "supplierName", "宝钢国贸",
                        "status", 0, "lotNo", "LOT-RM-001", "supplierLotNo", "SG-2026-08-001",
                        "furnaceLotNo", "F-2026-08-001"));
        MesTraceNodeBO qcTask = node(MesTraceNodeTypeEnum.QC_TASK, "QC20260901001", "完工检验", 4,
                snapshot("taskNo", "QC20260901001", "triggerPoint", "FQC", "bizType", "PROD_ORDER",
                        "bizNo", "MO20260901001", "matCode", "MAT-FIN", "lotNo", "LOT-FIN-001",
                        "qcPlanVersion", 3, "conclusion", "QUALIFIED", "status", "DONE",
                        "qty", new BigDecimal("100"), "qtyQualified", new BigDecimal("98"),
                        "qtyRejected", new BigDecimal("2"), "inspectEndTime",
                        LocalDateTime.of(2026, 9, 5, 10, 0, 0)));
        MesTraceNodeBO wipTxn = node(MesTraceNodeTypeEnum.WIP_TXN, "WTXN20260901001", "报工", 4,
                snapshot("txnNo", "WTXN20260901001", "txnType", "REPORT", "qty", new BigDecimal("100"),
                        "fromStatus", "RUNNING", "toStatus", "DONE", "bizType", "PROD_REPORT",
                        "bizNo", "PR20260901001", "txnTime", LocalDateTime.of(2026, 9, 4, 18, 0, 0)));
        MesTraceNodeBO opTask = node(MesTraceNodeTypeEnum.OP_TASK, "OT20260901001", "绕线", 4,
                snapshot("taskNo", "OT20260901001", "workOrderNo", "WO20260901001", "opCode", "OP010",
                        "opName", "绕线", "seq", 10, "status", "DONE", "plannedQty", new BigDecimal("100"),
                        "completedQty", new BigDecimal("100"),
                        "actualStartTime", LocalDateTime.of(2026, 9, 3, 8, 0, 0),
                        "actualEndTime", LocalDateTime.of(2026, 9, 3, 16, 0, 0)));
        MesTraceNodeBO customer = node(MesTraceNodeTypeEnum.CUSTOMER, "CUS001", "宁波风电", 7,
                snapshot("customerId", 9L, "customerCode", "CUS001", "customerName", "宁波风电"));
        MesTraceNodeBO salesOut = node(MesTraceNodeTypeEnum.OUT_ORDER, "OUT20260901002", "销售发货出库", 6,
                snapshot("outNo", "OUT20260901002", "bizType", "SALES_DELIVERY_OUT",
                        "qty", new BigDecimal("60"),
                        "txnTime", LocalDateTime.of(2026, 9, 8, 9, 0, 0), "customerId", 9L),
                customer);
        MesTraceNodeBO finishedBatch = node(MesTraceNodeTypeEnum.BATCH, "LOT-FIN-001", "永磁电机", 5,
                snapshot("batchId", 200L, "matId", 20L, "matCode", "MAT-FIN", "matName", "永磁电机",
                        "lotNo", "LOT-FIN-001", "produceDate", LocalDate.of(2026, 9, 4),
                        "qty", new BigDecimal("100")),
                salesOut);
        MesTraceNodeBO inOrder = node(MesTraceNodeTypeEnum.IN_ORDER, "IN20260901001", "生产完工入库", 4,
                snapshot("inNo", "IN20260901001", "bizType", "PROD_FINISH_IN", "status", "DONE",
                        "whId", 5L, "sourceBizNo", "MO20260901001", "qty", new BigDecimal("100")),
                finishedBatch);
        MesTraceNodeBO prodOrder = node(MesTraceNodeTypeEnum.PROD_ORDER, "MO20260901001", "永磁电机", 3,
                snapshot("orderNo", "MO20260901001", "matId", 20L, "matCode", "MAT-FIN", "matName", "永磁电机",
                        "qty", new BigDecimal("100"), "completedQty", new BigDecimal("100"), "status", "DONE",
                        "kitStatus", "FULL_KIT", "bomVersion", "V1.2", "routeVersion", "R1.0",
                        "workOrderNos", List.of("WO20260901001")),
                opTask, qcTask, wipTxn, inOrder);
        MesTraceNodeBO issueOut = node(MesTraceNodeTypeEnum.OUT_ORDER, "OUT20260901001", "生产领料出库", 2,
                snapshot("outNo", "OUT20260901001", "bizType", "PROD_ISSUE_OUT", "status", "DONE",
                        "materialReqNo", "MR20260901001", "qty", new BigDecimal("50"),
                        "txnTime", LocalDateTime.of(2026, 9, 2, 9, 0, 0)),
                prodOrder);
        return node(MesTraceNodeTypeEnum.BATCH, "LOT-RM-001", "冷轧钢板", 1,
                snapshot("batchId", 100L, "matId", 10L, "matCode", "MAT-RM", "matName", "冷轧钢板",
                        "lotNo", "LOT-RM-001", "supplierId", 1L, "supplierLotNo", "SG-2026-08-001",
                        "furnaceLotNo", "F-2026-08-001", "produceDate", LocalDate.of(2026, 8, 1)),
                issueOut, supplier);
    }

    @Test
    public void testFlatten_levelPathAndParent() {
        List<TraceExcelRowRespVO> rows = MesTraceExcelFlattener.flatten(fullChain(), "正向", "批次", "LOT-RM-001");

        // 九类节点全部落到行里：1 原料批次 + 1 领料出库 + 1 工单 + 4 工单子节点 + 1 成品批次
        // + 1 销售出库 + 1 客户 + 1 供应商 = 11
        assertEquals(11, rows.size());
        TraceExcelRowRespVO root = rows.get(0);
        assertEquals(1, root.getLevel());
        assertEquals("1", root.getPath());
        assertEquals("LOT-RM-001", root.getBizNo());
        assertNull(root.getParentBizNo(), "起点行没有上级");
        assertEquals("批次", root.getNodeTypeName());
        assertEquals("正向", root.getDirectionName());
        assertEquals("批次", root.getStartTypeName());
        assertEquals("LOT-RM-001", root.getStartValue());

        TraceExcelRowRespVO issue = rows.get(1);
        assertEquals(2, issue.getLevel());
        assertEquals("1.1", issue.getPath());
        assertEquals("LOT-RM-001", issue.getParentBizNo());
        // 起点信息只在首行给出，避免每行重复刷屏
        assertNull(issue.getStartTypeName());

        TraceExcelRowRespVO opTask = rows.get(3);
        assertEquals(4, opTask.getLevel());
        assertEquals("1.1.1.1", opTask.getPath());
        assertEquals("MO20260901001", opTask.getParentBizNo());
        // 末级节点（客户、供应商）路径最深，层级列能还原父子关系
        // 前序遍历下标：0 原料批次 1 领料出库 2 工单 3~6 工单四子节点 7 成品批次 8 销售出库 9 客户 10 供应商
        TraceExcelRowRespVO customer = rows.get(9);
        assertEquals("客户", customer.getNodeTypeName());
        assertEquals(7, customer.getLevel());
        assertEquals("1.1.1.4.1.1.1", customer.getPath());
        assertEquals("OUT20260901002", customer.getParentBizNo());
    }

    @Test
    public void testFlatten_enumTranslatedToChinese() {
        List<TraceExcelRowRespVO> rows = MesTraceExcelFlattener.flatten(fullChain(), "反向", "批次", "LOT-FIN-001");

        assertTrue(rows.stream().noneMatch(row -> row.getNodeTypeName() == null), "节点类型必须都有中文");
        rows.forEach(row -> {
            assertFalse("BATCH".equals(row.getNodeTypeName()), "节点类型不能是原始枚举值");
            assertFalse("QUALIFIED".equals(row.getConclusionName()), "质检结论不能是原始枚举值");
        });

        // 工单：状态 + 齐套 + 两个版本号
        TraceExcelRowRespVO prodOrder = rowOf(rows, "MO20260901001");
        assertEquals("已完工（齐套：齐套）", prodOrder.getStatusName());
        assertEquals("V1.2", prodOrder.getBomVersion());
        assertEquals("R1.0", prodOrder.getRouteVersion());

        // 工序任务：状态与工序名单独成列；快照无 qty 时数量回退到完成数
        TraceExcelRowRespVO opTask = rowOf(rows, "OT20260901001");
        assertEquals("工序任务", opTask.getNodeTypeName());
        assertEquals("已完工", opTask.getStatusName());
        assertEquals("绕线", opTask.getOpName());
        assertEquals(new BigDecimal("100"), opTask.getQty());
        assertEquals("2026-09-03 16:00:00", opTask.getBizTime());

        // 质检任务：检验点、结论、方案版本
        TraceExcelRowRespVO qcTask = rowOf(rows, "QC20260901001");
        assertEquals("完工检验", qcTask.getTriggerPointName());
        assertEquals("合格", qcTask.getConclusionName());
        assertEquals("3", qcTask.getQcPlanVersion());
        assertEquals("已完成", qcTask.getStatusName());

        // 在制流水：事务类型 + 来源业务类型
        TraceExcelRowRespVO wipTxn = rowOf(rows, "WTXN20260901001");
        assertEquals("在制流水", wipTxn.getNodeTypeName());
        assertEquals("报工（生产报工）", wipTxn.getStatusName());
        assertEquals("2026-09-04 18:00:00", wipTxn.getBizTime());

        // WMS 单据：状态带上业务类型，一行就能看出是领料出库还是销售发货
        assertEquals("已过账（生产领料出库）", rowOf(rows, "OUT20260901001").getStatusName());
        assertEquals("已过账（生产完工入库）", rowOf(rows, "IN20260901001").getStatusName());
        // 销售出库节点由发货流水聚合而成，快照里没有 status 键（与正向解析器 buildSalesOutNode 的口径一致），
        // 此时只出业务类型、不硬凑状态，符合拍平器"缺键留空、不猜默认值"的约束
        assertEquals("销售发货出库", rowOf(rows, "OUT20260901002").getStatusName());

        // 批次：供应商与供应商批次；供应商节点启停状态译成中文
        TraceExcelRowRespVO rawBatch = rows.get(0);
        assertEquals("LOT-RM-001", rawBatch.getLotNo());
        assertEquals("SG-2026-08-001", rawBatch.getSupplierLotNo());
        assertEquals("F-2026-08-001", rawBatch.getFurnaceLotNo());
        assertEquals("2026-08-01", rawBatch.getBizTime(), "批次无过账时间时退到生产日期");
        TraceExcelRowRespVO supplier = rowOf(rows, "SUP001");
        assertEquals("供应商", supplier.getNodeTypeName());
        assertEquals("宝钢国贸", supplier.getSupplierName());
        assertEquals("开启", supplier.getStatusName());
        assertEquals("SG-2026-08-001", supplier.getSupplierLotNo());
        assertEquals("宁波风电", rowOf(rows, "CUS001").getCustomerName());
    }

    @Test
    public void testFlatten_snapshotColumnKeepsAllKeys() {
        List<TraceExcelRowRespVO> rows = MesTraceExcelFlattener.flatten(fullChain(), "正向", "批次", "LOT-RM-001");

        // 固定列没覆盖的键（工序序号、合格数、来源单号）必须能在关键快照列里找到，导出不丢信息
        String opTaskSnapshot = rowOf(rows, "OT20260901001").getSnapshotText();
        assertTrue(opTaskSnapshot.contains("seq=10"), opTaskSnapshot);
        assertTrue(opTaskSnapshot.contains("plannedQty=100"), opTaskSnapshot);
        String qcSnapshot = rowOf(rows, "QC20260901001").getSnapshotText();
        assertTrue(qcSnapshot.contains("qtyRejected=2"), qcSnapshot);
        assertTrue(qcSnapshot.contains("inspectEndTime=2026-09-05 10:00:00"), qcSnapshot);
        // 集合值拍成 / 分隔，空值不落进快照列
        assertTrue(rowOf(rows, "MO20260901001").getSnapshotText().contains("workOrderNos=WO20260901001"));
        assertFalse(rowOf(rows, "OUT20260901002").getSnapshotText().contains("customerId=null"));
    }

    @Test
    public void testFlatten_truncatedAndEmptyTree() {
        MesTraceNodeBO root = fullChain();
        root.setTruncated(true);
        List<TraceExcelRowRespVO> rows = MesTraceExcelFlattener.flatten(root, "正向", "生产工单", "MO20260901001");
        assertEquals("是（已达深度上限）", rows.get(0).getTruncatedName());
        assertEquals("否", rows.get(1).getTruncatedName());
        assertEquals("生产工单", rows.get(0).getStartTypeName());

        // 空树与无快照节点都不能抛异常：解析器某些分支只 set 了 bizNo
        assertTrue(MesTraceExcelFlattener.flatten(null, "正向", "批次", "X").isEmpty());
        MesTraceNodeBO bare = new MesTraceNodeBO().setNodeType(MesTraceNodeTypeEnum.BATCH.name())
                .setBizNo("LOT-X").setDepth(1);
        List<TraceExcelRowRespVO> bareRows = MesTraceExcelFlattener.flatten(bare, "反向", "批次", "LOT-X");
        assertEquals(1, bareRows.size());
        assertEquals("LOT-X", bareRows.get(0).getLotNo(), "批次节点 bizNo 即批次号，快照缺 lotNo 时回退");
        assertNull(bareRows.get(0).getSnapshotText());
    }

    private static TraceExcelRowRespVO rowOf(List<TraceExcelRowRespVO> rows, String bizNo) {
        return rows.stream().filter(row -> bizNo.equals(row.getBizNo())).findFirst()
                .orElseThrow(() -> new AssertionError("导出行缺失：" + bizNo));
    }

}
