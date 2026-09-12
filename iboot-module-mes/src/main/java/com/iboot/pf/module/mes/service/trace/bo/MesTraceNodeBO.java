package com.iboot.pf.module.mes.service.trace.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批次追溯树节点 BO。
 *
 * <p>六类节点（见 {@link com.iboot.pf.module.mes.enums.MesTraceNodeTypeEnum}）统一用这一个结构：
 * {@code nodeType} + {@code bizNo} 定位节点，{@code snapshot} 装各节点自己的关键字段快照
 * （物料、批次、数量、时间、版本号等），{@code children} 挂下一层。
 */
@Schema(description = "管理后台 - 批次追溯树节点")
@Data
@Accessors(chain = true)
public class MesTraceNodeBO {

    /** 节点类型。 */
    @Schema(description = "节点类型：BATCH、OUT_ORDER、PROD_ORDER、OP_TASK、IN_ORDER、CUSTOMER", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATCH")
    private String nodeType;

    /** 业务单号或批次号：节点的自然键，同层用它去重。 */
    @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "OUT20260901001")
    private String bizNo;

    /** 节点名称：物料名称、客户名称等，展示用。 */
    @Schema(description = "节点名称", example = "钢板")
    private String nodeName;

    /** 关键字段快照，节点类型不同内容不同，供前端透出。 */
    @Schema(description = "关键字段快照")
    private Map<String, Object> snapshot;

    /** 子节点。 */
    @Schema(description = "子节点")
    private List<MesTraceNodeBO> children = new ArrayList<>();

    /** 层级深度，根节点为 1；超过深度上限被截断时子节点为空。 */
    @Schema(description = "层级深度", example = "1")
    private Integer depth;

    /** 是否因超过深度上限被截断。 */
    @Schema(description = "是否深度截断", example = "false")
    private Boolean truncated;

}
