package com.iboot.pf.module.mdm.controller.admin.matalt.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 替代料分页 Request VO")
@Data
public class MatAltPageReqVO extends PageParam {

    @Schema(description = "物料编号", example = "19327")
    private Long matId;

    @Schema(description = "替代物料编号", example = "26792")
    private Long altMatId;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}