package com.iboot.pf.module.aps.controller.admin.shortage.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程缺料清单分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - APS-排程缺料清单分页 Request VO")
@Data
public class ShortagePageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "1")
    private Long versionId;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "短缺物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "状态", example = "OPEN")
    private String status;

    @Schema(description = "需求到位时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] needDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
