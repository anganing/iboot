package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;

@Schema(description = "管理后台 - MES-领退补料申请新增/修改 Request VO")
@Data
public class MaterialReqSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10773")
    private Long id;

    @Schema(description = "领退补料申请单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "领退补料申请单号不能为空")
    private String reqNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19224")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "生产工单号快照不能为空")
    private String prodOrderNo;

    @Schema(description = "作业工单编号，0表示工单级申请", requiredMode = Schema.RequiredMode.REQUIRED, example = "19058")
    @NotNull(message = "作业工单编号，0表示工单级申请不能为空")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序级申请", requiredMode = Schema.RequiredMode.REQUIRED, example = "9855")
    @NotNull(message = "工序任务编号，0表示非工序级申请不能为空")
    private Long opTaskId;

    @Schema(description = "申请类型：ISSUE、RETURN、SUPPLEMENT", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "申请类型：ISSUE、RETURN、SUPPLEMENT不能为空")
    private String reqType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12027")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19726")
    @NotNull(message = "目标仓库或来源仓库编号不能为空")
    private Long whId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "11633")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "20757")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "WMS生成的出入库单号")
    private String wmsBizNo;

    @Schema(description = "状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}