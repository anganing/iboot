package com.iboot.pf.module.mes.controller.admin.rework.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-返工任务分页 Request VO")
@Data
public class ReworkPageReqVO extends PageParam {

    @Schema(description = "返工任务编号")
    private String reworkNo;

    @Schema(description = "来源质检任务编号", example = "31445")
    private Long sourceQcTaskId;

    @Schema(description = "生产工单编号", example = "19246")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "16920")
    private Long workOrderId;

    @Schema(description = "返工起始工序任务编号", example = "3226")
    private Long opTaskId;

    @Schema(description = "返工数量")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号", example = "15518")
    private Long routeId;

    @Schema(description = "返工工艺路线版本")
    private String routeVersion;

    @Schema(description = "状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED", example = "1")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}