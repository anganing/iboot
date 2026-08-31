package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 检验项目分页 Request VO。
 *
 * <p>检验项通常按方案查看，方案编号是最常用的筛选条件。
 */
@Schema(description = "管理后台 - MES-检验项目分页 Request VO")
@Data
public class QcItemPageReqVO extends PageParam {

    @Schema(description = "检验方案编号", example = "1")
    private Long planId;

    @Schema(description = "检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称")
    private String itemName;

    @Schema(description = "检验项类型", example = "NUMERIC")
    private String itemType;

    @Schema(description = "是否必检", example = "true")
    private Boolean requiredFlag;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
