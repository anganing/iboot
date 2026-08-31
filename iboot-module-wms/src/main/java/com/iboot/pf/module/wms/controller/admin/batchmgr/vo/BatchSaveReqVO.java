package com.iboot.pf.module.wms.controller.admin.batchmgr.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;

@Schema(description = "管理后台 - WMS-批次主档新增/修改 Request VO")
@Data
public class BatchSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "批次号；无批次物料使用默认批次", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "批次号；无批次物料使用默认批次不能为空")
    private String lotNo;

    @Schema(description = "序列号；SN物料以数量为1的批次承载")
    private String snNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商批次号")
    private String supplierLotNo;

    @Schema(description = "炉批号")
    private String furnaceLotNo;

    @Schema(description = "生产日期")
    private LocalDate produceDate;

    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}