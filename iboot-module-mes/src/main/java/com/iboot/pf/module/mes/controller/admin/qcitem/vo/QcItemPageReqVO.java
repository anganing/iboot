package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-检验项目分页 Request VO")
@Data
public class QcItemPageReqVO extends PageParam {

    @Schema(description = "检验方案编号", example = "25866")
    private Long planId;

    @Schema(description = "检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称", example = "李四")
    private String itemName;

    @Schema(description = "检验项类型：NUMERIC、TEXT、BOOLEAN", example = "1")
    private String itemType;

    @Schema(description = "检验值单位")
    private String unit;

    @Schema(description = "标准值")
    private String stdValue;

    @Schema(description = "数值上限")
    private BigDecimal upperLimit;

    @Schema(description = "数值下限")
    private BigDecimal lowerLimit;

    @Schema(description = "是否必检")
    private Boolean requiredFlag;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态：0-启用，1-停用", example = "2")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}