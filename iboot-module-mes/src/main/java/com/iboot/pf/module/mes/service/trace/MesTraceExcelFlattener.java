package com.iboot.pf.module.mes.service.trace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExcelRowRespVO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 追溯链路树 → Excel 行 的拍平器（T5-03 导出端点专用）。
 *
 * <p>放 Service 侧而不是 Controller：08 文档"补全逻辑写在 Service 而非 Controller"，
 * 导出复用的正是查询返回的那棵树，翻译与组装全在这里做完，Controller 只负责写响应流。
 *
 * <p>两条实现约束：
 * <ol>
 *   <li><b>不改解析器</b>（T5-01/T5-02 独占），只读 {@link MesTraceNodeBO} 的 snapshot 取值，
 *       因此所有字段抽取都要容错——快照缺键就留空，不猜、不补默认值。</li>
 *   <li><b>枚举一律译成中文</b>，Excel 交给客户审核，不能出现 {@code REJECTED} 这类原始值；
 *       译不出来的取值原样输出（宁缺不假），并始终保留"关键快照"列兜住全量信息。</li>
 * </ol>
 */
final class MesTraceExcelFlattener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MesTraceExcelFlattener() {
    }

    /**
     * 前序遍历拍平整棵树。
     *
     * @param root       链路树根（可能是多根时解析器包的虚拟根，一并作为一行输出，便于核对起点）
     * @param direction  方向中文名，写进每行方便多份材料区分
     * @param startType  起点类型中文名
     * @param startValue 起点值
     */
    static List<TraceExcelRowRespVO> flatten(MesTraceNodeBO root, String direction,
                                             String startType, String startValue) {
        List<TraceExcelRowRespVO> rows = new ArrayList<>();
        if (root == null) {
            return rows;
        }
        appendNode(rows, root, null, 1, "1", direction, startType, startValue);
        return rows;
    }

    private static void appendNode(List<TraceExcelRowRespVO> rows, MesTraceNodeBO node, MesTraceNodeBO parent,
                                   int level, String path, String direction, String startType, String startValue) {
        rows.add(buildRow(node, parent, level, path, direction, startType, startValue));
        if (CollUtil.isEmpty(node.getChildren())) {
            return;
        }
        // 同一父节点下按出现顺序编号，路径列可还原父子关系（Excel 没有树形缩进）
        int index = 0;
        for (MesTraceNodeBO child : node.getChildren()) {
            if (child == null) {
                continue;
            }
            index++;
            appendNode(rows, child, node, level + 1, path + "." + index,
                    direction, startType, startValue);
        }
    }

    private static TraceExcelRowRespVO buildRow(MesTraceNodeBO node, MesTraceNodeBO parent,
                                                int level, String path,
                                                String direction, String startType, String startValue) {
        Map<String, Object> snap = node.getSnapshot() == null ? Map.of() : node.getSnapshot();
        String nodeType = node.getNodeType();

        TraceExcelRowRespVO row = new TraceExcelRowRespVO();
        row.setLevel(level);
        row.setPath(path);
        row.setParentBizNo(parent == null ? null : parent.getBizNo());
        row.setNodeTypeName(enumLabel(MesTraceNodeTypeEnum.class, nodeType, MesTraceNodeTypeEnum::getName));
        row.setBizNo(node.getBizNo());
        row.setNodeName(node.getNodeName());
        row.setDirectionName(direction);
        row.setStartTypeName(level == 1 ? startType : null);
        row.setStartValue(level == 1 ? startValue : null);

        row.setMatCode(str(snap.get("matCode")));
        row.setMatName(str(snap.get("matName")));
        row.setLotNo(lotNo(node, snap));
        row.setQty(qty(nodeType, snap));
        row.setBizTime(timeText(snap));
        row.setOpName(opName(nodeType, snap));
        row.setCustomerName(customerName(nodeType, snap));
        row.setSupplierName(supplierName(snap));
        row.setSupplierLotNo(str(snap.get("supplierLotNo")));
        row.setFurnaceLotNo(str(snap.get("furnaceLotNo")));
        row.setBomVersion(str(snap.get("bomVersion")));
        row.setRouteVersion(str(snap.get("routeVersion")));
        row.setTriggerPointName(enumLabel(MesQcTriggerPointEnum.class, str(snap.get("triggerPoint")),
                MesQcTriggerPointEnum::getName));
        row.setConclusionName(enumLabel(MesQcConclusionEnum.class, str(snap.get("conclusion")),
                MesQcConclusionEnum::getName));
        row.setQcPlanVersion(str(snap.get("qcPlanVersion")));
        row.setStatusName(statusName(nodeType, snap));
        row.setTruncatedName(Boolean.TRUE.equals(node.getTruncated()) ? "是（已达深度上限）" : "否");
        row.setSnapshotText(snapshotText(snap));
        return row;
    }

    // ========== 按节点类型分支的字段口径 ==========

    /** 批次节点 bizNo 就是批次号；其他节点取快照里的 lotNo。 */
    private static String lotNo(MesTraceNodeBO node, Map<String, Object> snap) {
        String lotNo = str(snap.get("lotNo"));
        if (StrUtil.isNotBlank(lotNo)) {
            return lotNo;
        }
        return MesTraceNodeTypeEnum.BATCH.name().equals(node.getNodeType()) ? node.getBizNo() : null;
    }

    /** 工序列：工序任务取工序名，在制流水的节点名已是事务类型中文名，不重复占列。 */
    private static String opName(String nodeType, Map<String, Object> snap) {
        return MesTraceNodeTypeEnum.OP_TASK.name().equals(nodeType) ? str(snap.get("opName")) : null;
    }

    /**
     * 客户列：客户节点取自身快照的 customerName。
     *
     * <p>销售出库单节点上只有 customerId，正向解析器今天挂不上客户节点（T5-01 遗留 1：WMS 门面未暴露
     * customer_id），届时客户节点自己会带上 customerName，这里不提前为恒空的 customerId 引一次跨模块查询。
     */
    private static String customerName(String nodeType, Map<String, Object> snap) {
        return MesTraceNodeTypeEnum.CUSTOMER.name().equals(nodeType) ? str(snap.get("customerName")) : null;
    }

    /** 供应商列：优先名称，退到编码——批次节点快照里只有 supplierCode。 */
    private static String supplierName(Map<String, Object> snap) {
        String name = str(snap.get("supplierName"));
        return StrUtil.isNotBlank(name) ? name : str(snap.get("supplierCode"));
    }

    /**
     * 状态列按节点类型选枚举翻译：同名不同义的 status 分散在 MES 工单/任务、WMS 单据、质检任务、
     * 供应商启停四套取值域里，必须分派，否则会译错或直接译不出来。
     */
    private static String statusName(String nodeType, Map<String, Object> snap) {
        if (MesTraceNodeTypeEnum.PROD_ORDER.name().equals(nodeType)) {
            String status = enumLabel(MesProdOrderStatusEnum.class, str(snap.get("status")), MesProdOrderStatusEnum::getName);
            String kit = enumLabel(MesKitStatusEnum.class, str(snap.get("kitStatus")), MesKitStatusEnum::getName);
            return StrUtil.isBlank(kit) ? status : status + "（齐套：" + kit + "）";
        }
        if (MesTraceNodeTypeEnum.OP_TASK.name().equals(nodeType)) {
            return enumLabel(MesOpTaskStatusEnum.class, str(snap.get("status")), MesOpTaskStatusEnum::getName);
        }
        if (MesTraceNodeTypeEnum.QC_TASK.name().equals(nodeType)) {
            return enumLabel(MesQcTaskStatusEnum.class, str(snap.get("status")), MesQcTaskStatusEnum::getName);
        }
        if (MesTraceNodeTypeEnum.IN_ORDER.name().equals(nodeType)) {
            return orderStatusWithBizType(snap, WmsInBizTypeEnum.class, WmsInBizTypeEnum::getName);
        }
        if (MesTraceNodeTypeEnum.OUT_ORDER.name().equals(nodeType)) {
            return orderStatusWithBizType(snap, WmsOutBizTypeEnum.class, WmsOutBizTypeEnum::getName);
        }
        if (MesTraceNodeTypeEnum.SUPPLIER.name().equals(nodeType)) {
            return commonStatusName(snap.get("status"));
        }
        if (MesTraceNodeTypeEnum.WIP_TXN.name().equals(nodeType)) {
            String txnType = enumLabel(MesWipTxnTypeEnum.class, str(snap.get("txnType")), MesWipTxnTypeEnum::getName);
            String bizType = enumLabel(MesBizTypeEnum.class, str(snap.get("bizType")), MesBizTypeEnum::getName);
            return StrUtil.isBlank(bizType) ? txnType : txnType + "（" + bizType + "）";
        }
        return null;
    }

    /**
     * 数量列：批次 / 单据 / 工单 / 质检都带 qty，工序任务带的是 plannedQty + completedQty 两个键，
     * 统一回退到"完成数优先、无完成数退回计划数"，否则工序行的数量列会整列空掉。
     */
    private static BigDecimal qty(String nodeType, Map<String, Object> snap) {
        BigDecimal qty = decimal(snap.get("qty"));
        if (qty != null) {
            return qty;
        }
        BigDecimal completed = decimal(snap.get("completedQty"));
        return completed != null ? completed : decimal(snap.get("plannedQty"));
    }

    /** WMS 单据：状态 + 业务类型一起给，审核方看一行就知道是领料出库还是销售发货。 */
    private static <E extends Enum<E>> String orderStatusWithBizType(Map<String, Object> snap,
                                                                     Class<E> bizTypeEnum,
                                                                     Function<E, String> labelGetter) {
        String status = enumLabel(WmsOrderStatusEnum.class, str(snap.get("status")), WmsOrderStatusEnum::getName);
        String bizType = enumLabel(bizTypeEnum, str(snap.get("bizType")), labelGetter);
        if (StrUtil.isBlank(status)) {
            return bizType;
        }
        return StrUtil.isBlank(bizType) ? status : status + "（" + bizType + "）";
    }

    private static String commonStatusName(Object value) {
        if (!(value instanceof Integer status)) {
            return null;
        }
        for (CommonStatusEnum item : CommonStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item.getName();
            }
        }
        return String.valueOf(status);
    }

    /** 时间列按节点实际携带的字段取：过账/事务时间优先，其次实际完成、开始，最后生产日期。 */
    private static String timeText(Map<String, Object> snap) {
        for (String key : List.of("txnTime", "actualEndTime", "actualStartTime",
                "inspectEndTime", "inspectStartTime", "produceDate", "expiryDate")) {
            String text = dateText(snap.get(key));
            if (StrUtil.isNotBlank(text)) {
                return text;
            }
        }
        return null;
    }

    private static String dateText(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        if (value instanceof LocalDate date) {
            return date.format(DATE_FORMATTER);
        }
        return null;
    }

    /** 关键快照全量列：逐键拼 k=v，兜住固定列没覆盖到的信息（如工序序号、合格数、来源单号）。 */
    private static String snapshotText(Map<String, Object> snap) {
        if (snap.isEmpty()) {
            return null;
        }
        List<String> parts = new ArrayList<>(snap.size());
        snap.forEach((key, value) -> {
            String text = valueText(value);
            if (StrUtil.isNotBlank(text)) {
                parts.add(key + "=" + text);
            }
        });
        return StrUtil.join("; ", parts);
    }

    private static String valueText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        if (value instanceof LocalDate date) {
            return date.format(DATE_FORMATTER);
        }
        if (value instanceof Collection<?> collection) {
            return CollUtil.isEmpty(collection) ? null
                    : collection.stream().map(String::valueOf).filter(StrUtil::isNotBlank).reduce((a, b) -> a + "/" + b).orElse(null);
        }
        if (value instanceof Map<?, ?> map) {
            return map.isEmpty() ? null : String.valueOf(map);
        }
        return String.valueOf(value);
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return null;
    }

    /**
     * 枚举中文名查找：MES/WMS 各枚举统一是 {@code name() + getName()} 的写法，
     * 泛型一次实现，避免九类节点各写一遍 switch。译不出原样返回，不编造中文。
     */
    private static <E extends Enum<E>> String enumLabel(Class<E> enumType, String value, Function<E, String> labelGetter) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (E constant : enumType.getEnumConstants()) {
            if (value.equals(constant.name())) {
                return labelGetter.apply(constant);
            }
        }
        return value;
    }

}
