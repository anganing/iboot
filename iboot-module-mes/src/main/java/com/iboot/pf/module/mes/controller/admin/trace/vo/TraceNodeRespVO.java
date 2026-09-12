package com.iboot.pf.module.mes.controller.admin.trace.vo;

import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 批次追溯树节点 Response VO。
 *
 * <p>结构与 Service 层 {@link MesTraceNodeBO} 一致，Controller 出口用独立 VO，
 * 遵循"Controller/VO 与 Service/BO 分层"的项目约定。
 */
@Schema(description = "管理后台 - 批次追溯树节点 Response VO")
@Data
public class TraceNodeRespVO {

    /** 节点类型：BATCH / OUT_ORDER / PROD_ORDER / OP_TASK / IN_ORDER / CUSTOMER。 */
    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATCH")
    private String nodeType;

    /** 业务单号或批次号。 */
    @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "OUT20260901001")
    private String bizNo;

    /** 节点名称。 */
    @Schema(description = "节点名称", example = "钢板")
    private String nodeName;

    /** 关键字段快照。 */
    @Schema(description = "关键字段快照")
    private Map<String, Object> snapshot;

    /** 层级深度。 */
    @Schema(description = "层级深度", example = "1")
    private Integer depth;

    /** 是否因超过深度上限被截断。 */
    @Schema(description = "是否深度截断", example = "false")
    private Boolean truncated;

    /** 子节点。 */
    @Schema(description = "子节点")
    private List<TraceNodeRespVO> children = new ArrayList<>();

    /** 从 Service 层 BO 递归转换。 */
    public static TraceNodeRespVO of(MesTraceNodeBO bo) {
        if (bo == null) {
            return null;
        }
        TraceNodeRespVO vo = new TraceNodeRespVO();
        vo.setNodeType(bo.getNodeType());
        vo.setBizNo(bo.getBizNo());
        vo.setNodeName(bo.getNodeName());
        vo.setSnapshot(bo.getSnapshot());
        vo.setDepth(bo.getDepth());
        vo.setTruncated(bo.getTruncated());
        if (bo.getChildren() != null) {
            vo.setChildren(bo.getChildren().stream().map(TraceNodeRespVO::of)
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
        return vo;
    }

}
