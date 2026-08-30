package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-不合格品处置单分页 Request VO")
@Data
public class DispositionPageReqVO extends PageParam {

    @Schema(description = "不合格处置单号")
    private String dispositionNo;

    @Schema(description = "质检任务编号", example = "27490")
    private Long taskId;

    @Schema(description = "处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS", example = "1")
    private String dispositionType;

    @Schema(description = "处置数量")
    private BigDecimal qty;

    @Schema(description = "联动目标业务类型", example = "1")
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    private String targetBizNo;

    @Schema(description = "审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] approveTime;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED", example = "1")
    private String status;

    @Schema(description = "处置幂等键")
    private String idempotencyKey;

    @Schema(description = "处置说明", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}