package com.iboot.pf.module.mdm.controller.admin.changeover.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 资源换型矩阵分页 Request VO")
@Data
public class ResChangeoverPageReqVO extends PageParam {

    @Schema(description = "资源编号")
    private Long resId;

    @Schema(description = "资源组")
    private String resGroup;

    @Schema(description = "换出换型分组")
    private String fromGroup;

    @Schema(description = "换入换型分组")
    private String toGroup;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
