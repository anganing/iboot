package com.iboot.pf.module.wms.controller.admin.invreservation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;

@Schema(description = "管理后台 - WMS-库存预留新增/修改 Request VO")
@Data
public class InvReservationSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "预留编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "预留编号不能为空")
    private String reservationNo;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务类型不能为空")
    @InEnum(WmsSourceBizTypeEnum.class)
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    private String bizNo;

    @Schema(description = "来源业务行号")
    private String bizLineNo;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库位编号不能为空")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "批次编号不能为空")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "容器编号，0表示散件不能为空")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatus;

    @Schema(description = "预留数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预留数量，基准单位不能为空")
    private BigDecimal qty;

    @Schema(description = "已释放或已核销数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已释放或已核销数量不能为空")
    private BigDecimal releasedQty;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsReservationStatusEnum.class)
    private String status;

    @Schema(description = "预留失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "预留幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "预留幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "备注")
    private String remark;

}
