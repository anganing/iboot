package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 齐套检查请求。
 *
 * <p>三档 {@code checkScope} 决定按哪一层展开需求，各自要带的 ID 不同：
 * <table>
 *     <tr><th>{@code checkScope}</th><th>要带的 ID</th><th>适用场景</th></tr>
 *     <tr><td>{@code PROD_ORDER}</td><td>只要 {@code id}</td><td>投产前按整单 BOM 粗判</td></tr>
 *     <tr><td>{@code WORK_ORDER}</td><td>{@code id} + {@code workOrderId}</td><td>分段投产，按该段覆盖的工序用料</td></tr>
 *     <tr><td>{@code OP_TASK}</td><td>{@code id} + {@code opTaskId}</td><td>单道工序开工前</td></tr>
 * </table>
 * 三档都要带生产工单编号：齐套快照以生产工单为主维度，缺了它这份快照挂不上任何单据。
 * 范围与对应 ID 对不上时由领域 Service 抛 {@code KIT_CHECK_SCOPE_TARGET_REQUIRED}。
 *
 * <p><b>刻意没有父项数量入参</b>：需求量按"工单数量 × 单位用量 ÷ (1 − 损耗率)"算，工单数量由服务端从工单
 * 或工序任务上读。让前端传就等于让界面自己决定检查多少件，快照就不再是工单的事实。
 *
 * <p>齐套是可反复执行的查询型动作，{@code idempotencyKey} 一般留空：服务端每次生成新的检查编号并按它派生键，
 * 因此天然不撞键、允许多次检查产生多份快照。只有想拿回上一次那份快照时才显式传同一个键。
 */
@Schema(description = "管理后台 - MES-生产工单齐套检查 Request VO")
@Data
public class ProdOrderKitCheckReqVO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long id;

    @Schema(description = "检查范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ORDER")
    @NotEmpty(message = "检查范围不能为空")
    @InEnum(MesKitCheckScopeEnum.class)
    private String checkScope;

    @Schema(description = "作业工单编号，检查范围为 WORK_ORDER 时必填", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，检查范围为 OP_TASK 时必填", example = "1")
    private Long opTaskId;

    @Schema(description = "取可用量的仓库编号，留空或 0 表示按全部仓库汇总", example = "1")
    private Long whId;

    @Schema(description = "幂等键，留空由服务端按工单号与检查编号派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

}
