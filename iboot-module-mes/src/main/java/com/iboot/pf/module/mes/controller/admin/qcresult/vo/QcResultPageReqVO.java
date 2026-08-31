package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 检验结果分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-检验结果分页 Request VO")
@Data
public class QcResultPageReqVO extends PageParam {

    @Schema(description = "质检任务编号", example = "1")
    private Long taskId;

    @Schema(description = "检验项目编号", example = "1")
    private Long itemId;

    @Schema(description = "检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称")
    private String itemName;

    @Schema(description = "是否合格", example = "true")
    private Boolean passFlag;

    @Schema(description = "检验员编号", example = "1")
    private Long inspectorId;

    @Schema(description = "项目结论", example = "PASS")
    private String conclusion;

    @Schema(description = "检验时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
