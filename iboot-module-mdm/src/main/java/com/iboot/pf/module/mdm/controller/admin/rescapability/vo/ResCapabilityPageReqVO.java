package com.iboot.pf.module.mdm.controller.admin.rescapability.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 资源能力分页 Request VO")
@Data
public class ResCapabilityPageReqVO extends PageParam {

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "标准工序编号", example = "1")
    private Long opId;

    @Schema(description = "是否首选资源")
    private Boolean preferredFlag;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
