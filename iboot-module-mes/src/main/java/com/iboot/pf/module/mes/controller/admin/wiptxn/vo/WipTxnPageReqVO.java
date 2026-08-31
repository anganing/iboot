package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 在制流转流水分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-在制流转流水分页 Request VO")
@Data
public class WipTxnPageReqVO extends PageParam {

    @Schema(description = "在制事务编号")
    private String txnNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "事务类型", example = "START")
    private String txnType;

    @Schema(description = "来源业务类型", example = "PROD_REPORT")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "事务发生时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] txnTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
