package com.iboot.pf.module.aps.controller.admin.load.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 资源负荷分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - APS-资源负荷分页 Request VO")
@Data
public class LoadPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "1")
    private Long versionId;

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "资源编码")
    private String resCode;

    @Schema(description = "资源名称")
    private String resName;

    @Schema(description = "只看超负荷", example = "true")
    private Boolean overloadOnly;

    @Schema(description = "负荷日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] workDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
