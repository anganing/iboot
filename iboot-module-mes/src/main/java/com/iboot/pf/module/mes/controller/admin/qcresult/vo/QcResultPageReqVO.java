package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-检验结果分页 Request VO")
@Data
public class QcResultPageReqVO extends PageParam {

    @Schema(description = "质检任务编号", example = "1159")
    private Long taskId;

    @Schema(description = "检验项目编号", example = "25386")
    private Long itemId;

    @Schema(description = "样本序号")
    private Integer sampleNo;

    @Schema(description = "检验项编码快照")
    private String itemCode;

    @Schema(description = "检验项名称快照", example = "赵六")
    private String itemName;

    @Schema(description = "实测值")
    private String actualValue;

    @Schema(description = "是否合格")
    private Boolean passFlag;

    @Schema(description = "检验员编号", example = "29231")
    private Long inspectorId;

    @Schema(description = "检验时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectTime;

    @Schema(description = "本项判定合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "本项判定不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "项目结论：PASS、FAIL")
    private String conclusion;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}