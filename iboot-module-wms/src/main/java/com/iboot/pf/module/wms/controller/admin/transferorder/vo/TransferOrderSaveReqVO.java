package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;

@Schema(description = "管理后台 - WMS-库存转移调拨单新增/修改 Request VO")
@Data
public class TransferOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21621")
    private Long id;

    @Schema(description = "转移或调拨单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "转移或调拨单号不能为空")
    private String transferNo;

    @Schema(description = "转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER不能为空")
    private String transferType;

    @Schema(description = "来源业务类型", example = "1")
    private String sourceType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18656")
    @NotNull(message = "来源库存组织编号不能为空")
    private Long fromInvOrgId;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "453")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long toInvOrgId;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17461")
    @NotNull(message = "来源仓库编号不能为空")
    private Long fromWhId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11884")
    @NotNull(message = "目标仓库编号不能为空")
    private Long toWhId;

    @Schema(description = "来源项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "25335")
    @NotNull(message = "来源项目编号，0表示公共库存不能为空")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "19928")
    @NotNull(message = "目标项目编号，0表示公共库存不能为空")
    private Long toProjectId;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备触发方式：NONE、WCS、AGV不能为空")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}