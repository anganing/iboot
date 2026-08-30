package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-领退补料申请分页 Request VO")
@Data
public class MaterialReqPageReqVO extends PageParam {

    @Schema(description = "领退补料申请单号")
    private String reqNo;

    @Schema(description = "生产工单编号", example = "19224")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单编号，0表示工单级申请", example = "19058")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序级申请", example = "9855")
    private Long opTaskId;

    @Schema(description = "申请类型：ISSUE、RETURN、SUPPLEMENT", example = "1")
    private String reqType;

    @Schema(description = "库存组织编号", example = "12027")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", example = "19726")
    private Long whId;

    @Schema(description = "项目编号，0表示无项目", example = "11633")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "20757")
    private Long projectTaskId;

    @Schema(description = "WMS生成的出入库单号")
    private String wmsBizNo;

    @Schema(description = "状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED", example = "1")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}