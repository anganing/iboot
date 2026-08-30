package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-库存转移调拨单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TransferOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21621")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "转移或调拨单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("转移或调拨单号")
    private String transferNo;

    @Schema(description = "转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER")
    private String transferType;

    @Schema(description = "来源业务类型", example = "1")
    @ExcelProperty("来源业务类型")
    private String sourceType;

    @Schema(description = "来源业务单号")
    @ExcelProperty("来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18656")
    @ExcelProperty("来源库存组织编号")
    private Long fromInvOrgId;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "453")
    @ExcelProperty("目标库存组织编号")
    private Long toInvOrgId;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17461")
    @ExcelProperty("来源仓库编号")
    private Long fromWhId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11884")
    @ExcelProperty("目标仓库编号")
    private Long toWhId;

    @Schema(description = "来源项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "25335")
    @ExcelProperty("来源项目编号，0表示公共库存")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "19928")
    @ExcelProperty("目标项目编号，0表示公共库存")
    private Long toProjectId;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}