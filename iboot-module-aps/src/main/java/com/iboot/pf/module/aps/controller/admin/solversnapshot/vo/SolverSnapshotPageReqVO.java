package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 求解快照分页 Request VO")
@Data
public class SolverSnapshotPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "26936")
    private Long versionId;

    @Schema(description = "快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR", example = "2")
    private String snapshotType;

    @Schema(description = "Timefold求解输入、结果或约束命中快照")
    private String snapshotJson;

    @Schema(description = "快照内容校验值")
    private String checksum;

    @Schema(description = "快照生成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}