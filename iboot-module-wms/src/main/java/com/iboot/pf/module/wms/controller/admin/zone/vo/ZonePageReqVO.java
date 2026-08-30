package com.iboot.pf.module.wms.controller.admin.zone.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库区分页 Request VO")
@Data
public class ZonePageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "32765")
    private Long whId;

    @Schema(description = "库区编码")
    private String code;

    @Schema(description = "库区名称", example = "张三")
    private String name;

    @Schema(description = "库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING", example = "1")
    private String zoneType;

    @Schema(description = "是否允许混放物料")
    private Boolean allowMixedMat;

    @Schema(description = "是否允许混放批次")
    private Boolean allowMixedLot;

    @Schema(description = "温区要求")
    private String tempRange;

    @Schema(description = "状态：0-启用，1-停用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}