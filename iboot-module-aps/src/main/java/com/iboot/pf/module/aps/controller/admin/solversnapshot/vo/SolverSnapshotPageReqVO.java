package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 求解快照分页 Request VO。 */
@Schema(description = "管理后台 - APS-求解快照分页 Request VO")
@Data
public class SolverSnapshotPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "1")
    private Long versionId;

    @Schema(description = "快照类型", example = "INPUT")
    private String snapshotType;

    @Schema(description = "快照内容校验值")
    private String checksum;

    @Schema(description = "快照生成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
