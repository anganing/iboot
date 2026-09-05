package com.iboot.pf.module.mes.service.optask.gate;

import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import lombok.Data;

import java.time.LocalDate;

/**
 * 开工门禁的上下文。
 *
 * <p>只承载"入参"与"跨校验项共享的少量派生数据"，各校验项自己需要的明细各自批量取——
 * 把所有数据都塞进上下文会让它随校验项增加无限膨胀，而且校验项没启用时那些取数都白做了。
 *
 * <p>{@link #routeOp} 由编排者统一加载一次：它是门禁的配置来源（{@code fai_flag} 决定首件校验
 * 启不启用、{@code op_id} 是技能要求的挂点），编排者判断哪些项启用时本来就要读它。
 */
@Data
public class MesStartGateContext {

    /**
     * 操作者用户编号，0 或空表示未指定。
     *
     * <p>调用方给了以它为准（现场换人常有），留空时由编排者从 {@code mes_dispatch_task} 取。
     */
    private Long operatorId;

    /** 校验基准日期，默认当天。排班与技能有效期都按这一天判定。 */
    private LocalDate checkDate = LocalDate.now();

    /** 本工序任务对应的路线工序，由编排者加载；路线工序查不到时为空。 */
    private RouteOpRespDTO routeOp;

    /** 路线工序是否要求首件检验。路线工序查不到时按不要求处理。 */
    public boolean faiRequired() {
        return routeOp != null && Boolean.TRUE.equals(routeOp.getFaiFlag());
    }

    /** 标准工序编号，技能要求挂在它上面而不是路线工序上。 */
    public Long opId() {
        return routeOp == null ? null : routeOp.getOpId();
    }

}
