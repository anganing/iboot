package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.module.wms.dal.dataobject.countorderline.CountOrderLineDO;

@Schema(description = "管理后台 - WMS-盘点单新增/修改 Request VO")
@Data
public class CountOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19625")
    private Long id;

    @Schema(description = "盘点单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "盘点单号不能为空")
    private String countNo;

    @Schema(description = "盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC不能为空")
    private String countType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21976")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6193")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库区编号，0表示全部库区", requiredMode = Schema.RequiredMode.REQUIRED, example = "23296")
    @NotNull(message = "库区编号，0表示全部库区不能为空")
    private Long zoneId;

    @Schema(description = "项目编号，0表示不限项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "19244")
    @NotNull(message = "项目编号，0表示不限项目不能为空")
    private Long projectId;

    @Schema(description = "账面快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账面快照时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "是否盲盘", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否盲盘不能为空")
    private Boolean blindCountFlag;

    @Schema(description = "状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}