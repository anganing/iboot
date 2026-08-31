package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 物料投影分页 Request VO。 */
@Schema(description = "管理后台 - APS-物料投影分页 Request VO")
@Data
public class ProjectionMatPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "MDM 物料编号", example = "1")
    private Long sourceMatId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "物料类型", example = "RAW")
    private String matType;

    @Schema(description = "是否批次管控")
    private Boolean batchFlag;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

}
