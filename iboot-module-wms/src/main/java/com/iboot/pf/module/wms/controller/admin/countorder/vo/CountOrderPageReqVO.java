package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-盘点单分页 Request VO")
@Data
public class CountOrderPageReqVO extends PageParam {

    @Schema(description = "盘点单号")
    private String countNo;

    @Schema(description = "盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC", example = "1")
    private String countType;

    @Schema(description = "库存组织编号", example = "21976")
    private Long invOrgId;

    @Schema(description = "仓库编号", example = "6193")
    private Long whId;

    @Schema(description = "库区编号，0表示全部库区", example = "23296")
    private Long zoneId;

    @Schema(description = "项目编号，0表示不限项目", example = "19244")
    private Long projectId;

    @Schema(description = "账面快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "是否盲盘")
    private Boolean blindCountFlag;

    @Schema(description = "状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED", example = "2")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}