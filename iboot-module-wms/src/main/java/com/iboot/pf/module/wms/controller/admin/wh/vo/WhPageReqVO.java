package com.iboot.pf.module.wms.controller.admin.wh.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-仓库分页 Request VO")
@Data
public class WhPageReqVO extends PageParam {

    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "仓库编码")
    private String code;

    @Schema(description = "仓库名称")
    private String name;

    @Schema(description = "仓库类型", example = "ASRS")
    private String whType;

    @Schema(description = "管理方式", example = "MANUAL")
    private String managedBy;

    @Schema(description = "外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
