package com.iboot.pf.module.mes.service.kitsnapshot.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 齐套检查参数。
 *
 * <p>**故意不带父项数量**：需求量按"工单数量 × 单位用量 ÷ (1 - 损耗率)"算，工单数量由服务端从工单
 * （或工序任务）上读。让调用方传进来就等于让它自己决定检查多少件，快照就不再是工单的事实。
 *
 * <p>{@code checkScope} 决定按哪一层展开需求，三档的适用场景见
 * {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"流程三：齐套检查"：
 * {@code PROD_ORDER} 投产前粗判、{@code WORK_ORDER} 分段投产、{@code OP_TASK} 开工门禁。
 *
 * <p>齐套是可重复执行的查询型动作，允许多次检查产生多份快照。幂等键留空时由服务端按
 * {@code {来源模块}:{工单号}:KIT_CHECK:{检查编号}} 派生，检查编号每次新生成因而天然不重复；
 * 调用方显式传同一个键时才会命中幂等，返回上一次的那份快照。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesKitCheckBO extends MesCommandReqDTO {

    /** 检查范围，必填，取 {@link com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum} 的枚举名。 */
    private String checkScope;

    /** 生产工单编号，必填。三档检查都要落在生产工单这个主维度上。 */
    private Long prodOrderId;

    /** 作业工单编号，{@code WORK_ORDER} 级必填，其余档次落 0。 */
    private Long workOrderId;

    /** 工序任务编号，{@code OP_TASK} 级必填，其余档次落 0。 */
    private Long opTaskId;

    /** 取可用量的仓库编号，为空或 0 表示不限仓库、按全部仓库汇总。 */
    private Long whId;

}
