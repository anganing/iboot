package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-在制流转流水分页 Request VO")
@Data
public class WipTxnPageReqVO extends PageParam {

    @Schema(description = "在制事务编号")
    private String txnNo;

    @Schema(description = "生产工单编号", example = "32266")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "12103")
    private Long workOrderId;

    @Schema(description = "工序任务编号", example = "26127")
    private Long opTaskId;

    @Schema(description = "事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP", example = "1")
    private String txnType;

    @Schema(description = "事务数量")
    private BigDecimal qty;

    @Schema(description = "变更前状态", example = "2")
    private String fromStatus;

    @Schema(description = "变更后状态", example = "1")
    private String toStatus;

    @Schema(description = "来源业务类型", example = "1")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "来源工序任务编号，0表示无", example = "14730")
    private Long fromOpTaskId;

    @Schema(description = "目标工序任务编号，0表示无", example = "31090")
    private Long toOpTaskId;

    @Schema(description = "事务发生时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] txnTime;

    @Schema(description = "事务幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}