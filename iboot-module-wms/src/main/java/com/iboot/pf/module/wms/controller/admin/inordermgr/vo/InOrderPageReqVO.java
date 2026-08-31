package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-入库单分页 Request VO")
@Data
public class InOrderPageReqVO extends PageParam {

    @Schema(description = "入库单号")
    private String inNo;

    @Schema(description = "入库业务类型", example = "PO_IN")
    private String bizType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "项目编号，0表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "目标仓库编号", example = "1")
    private Long whId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
