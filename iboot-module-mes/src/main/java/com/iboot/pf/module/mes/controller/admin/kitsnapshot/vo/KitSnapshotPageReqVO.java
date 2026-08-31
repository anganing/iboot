package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 齐套检查快照分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-齐套检查快照分页 Request VO")
@Data
public class KitSnapshotPageReqVO extends PageParam {

    @Schema(description = "齐套检查编号")
    private String checkNo;

    @Schema(description = "检查范围", example = "PROD_ORDER")
    private String checkScope;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "需求物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "检查结果", example = "FULL_KIT")
    private String result;

    @Schema(description = "检查时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] checkTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
