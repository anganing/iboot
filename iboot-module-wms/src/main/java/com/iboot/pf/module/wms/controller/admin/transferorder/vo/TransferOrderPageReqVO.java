package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存转移调拨单分页 Request VO")
@Data
public class TransferOrderPageReqVO extends PageParam {

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER", example = "2")
    private String transferType;

    @Schema(description = "来源业务类型", example = "1")
    private String sourceType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源库存组织编号", example = "18656")
    private Long fromInvOrgId;

    @Schema(description = "目标库存组织编号", example = "453")
    private Long toInvOrgId;

    @Schema(description = "来源仓库编号", example = "17461")
    private Long fromWhId;

    @Schema(description = "目标仓库编号", example = "11884")
    private Long toWhId;

    @Schema(description = "来源项目编号，0表示公共库存", example = "25335")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，0表示公共库存", example = "19928")
    private Long toProjectId;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", example = "2")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}