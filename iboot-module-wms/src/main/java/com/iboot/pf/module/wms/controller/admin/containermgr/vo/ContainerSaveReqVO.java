package com.iboot.pf.module.wms.controller.admin.containermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-容器主档新增/修改 Request VO")
@Data
public class ContainerSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5001")
    private Long id;

    @Schema(description = "容器编号或条码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "容器编号或条码不能为空")
    private String containerNo;

    @Schema(description = "容器类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9759")
    @NotNull(message = "容器类型编号不能为空")
    private Long typeId;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26915")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "当前仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22404")
    @NotNull(message = "当前仓库编号不能为空")
    private Long whId;

    @Schema(description = "当前库位编号，0表示未落位", requiredMode = Schema.RequiredMode.REQUIRED, example = "26975")
    @NotNull(message = "当前库位编号，0表示未落位不能为空")
    private Long locId;

    @Schema(description = "实际长度，单位毫米")
    private BigDecimal length;

    @Schema(description = "实际宽度，单位毫米")
    private BigDecimal width;

    @Schema(description = "实际高度，单位毫米")
    private BigDecimal height;

    @Schema(description = "实际毛重")
    private BigDecimal grossWeight;

    @Schema(description = "状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED不能为空")
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}