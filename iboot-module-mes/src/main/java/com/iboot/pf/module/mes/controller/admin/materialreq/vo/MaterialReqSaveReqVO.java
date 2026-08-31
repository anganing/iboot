package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 领退补料申请新增/修改 Request VO。
 *
 * <p>生产工单号快照、项目维度由后端从生产工单继承；WMS 单号由 WMS 回写，都不由前端提交。
 */
@Schema(description = "管理后台 - MES-领退补料申请新增/修改 Request VO")
@Data
public class MaterialReqSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "领退补料申请单号，留空由编码规则分配")
    @Size(max = 64, message = "申请单号长度不能超过 64")
    private String reqNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示工单级申请", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序级申请", example = "1")
    private Long opTaskId;

    @Schema(description = "申请类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ISSUE")
    @NotEmpty(message = "申请类型不能为空")
    @InEnum(MesMaterialReqTypeEnum.class)
    private String reqType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesMaterialReqStatusEnum.class)
    private String status;

    @Schema(description = "创建幂等键")
    @Size(max = 128, message = "创建幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
