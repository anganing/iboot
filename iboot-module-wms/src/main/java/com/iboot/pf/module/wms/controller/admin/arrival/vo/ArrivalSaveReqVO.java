package com.iboot.pf.module.wms.controller.admin.arrival.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.module.wms.dal.dataobject.arrivalline.ArrivalLineDO;

@Schema(description = "管理后台 - WMS-到货登记新增/修改 Request VO")
@Data
public class ArrivalSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "7095")
    private Long id;

    @Schema(description = "到货登记单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "到货登记单号不能为空")
    private String arrivalNo;

    @Schema(description = "来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER不能为空")
    private String sourceType;

    @Schema(description = "来源单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源单号不能为空")
    private String sourceNo;

    @Schema(description = "供应商编号", example = "19938")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商名称快照", example = "李四")
    private String supplierName;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7601")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20682")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28154")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    @Schema(description = "预计到货日期")
    private LocalDate expectDate;

    @Schema(description = "实际到货时间")
    private LocalDateTime arrivalTime;

    @Schema(description = "状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}