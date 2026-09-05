package com.iboot.pf.module.mes.service.prodreport.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报工提交参数。
 *
 * <p>报工是唯一往上推进数量的动作，一次报工推进三级（工序任务、作业工单、生产工单）。
 *
 * <p>幂等键<b>必填</b>，服务端不派生。格式 {@code UI:{taskNo}:REPORT:{seq}}，其中 {@code seq} 是
 * "这道工序的第几次报工"，由调用方（作业页、扫码端）在自己的重试作用域里给出。
 * 服务端按已有报工数派生会让每次重试都算出一个新的 seq，幂等当场失效——而报工是所有 MES 动作里
 * 最容易被重复提交的一个（扫码枪连扫、弱网重发）。键落在 {@code mes_prod_report.idempotency_key}，
 * 由唯一索引 {@code uk_mes_prod_report_idem_deleted} 兜住并发。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesSubmitReportBO extends MesCommandReqDTO {

    /** 工序任务编号，必填。生产工单与作业工单编号由它反查带出，调用方不必给。 */
    private Long opTaskId;

    /** 本次合格数，可为零但不能为负。三个数量不能全为零，否则这条报工没有意义。 */
    private BigDecimal qtyGood;

    /** 本次报废数。 */
    private BigDecimal qtyScrap;

    /**
     * 本次返工数。
     *
     * <p><b>只记在报工记录上，不进三级数量推进</b>：返工件既没合格也没报废，它还在返工任务里流转，
     * 最终要么合格要么报废。现在就把它算进产出，等返工完再算一次就重复计数了。
     * 返工件的归属在阶段 2 的返工任务里处理。
     */
    private BigDecimal qtyRework;

    /** 报工作业人员编号，可空落 0。 */
    private Long workerId;

    /** 生产资源编号，可空落 0。 */
    private Long resId;

    /** 班次编号，可空落 0。 */
    private Long shiftId;

    /** 本次作业开始时间，可空取报工时间。库列 NOT NULL。 */
    private LocalDateTime startTime;

    /** 本次作业结束时间，可空取报工时间，不能早于开始时间。库列 NOT NULL。 */
    private LocalDateTime endTime;

    /** 报工时间，可空取当前时间。 */
    private LocalDateTime reportTime;

    /** 备注。 */
    private String remark;

}
