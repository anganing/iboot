package com.iboot.pf.module.aps.controller.admin.version.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程版本分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - APS-排程版本分页 Request VO")
@Data
public class VersionPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "排程版本号", example = "1")
    private Integer versionNo;

    @Schema(description = "状态", example = "SOLVING")
    private String status;

    @Schema(description = "发布人员编号", example = "1")
    private Long publishedBy;

    @Schema(description = "发布时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] publishedTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
