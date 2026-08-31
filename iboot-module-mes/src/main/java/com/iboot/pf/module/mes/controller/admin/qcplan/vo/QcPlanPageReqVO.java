package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 检验方案分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-检验方案分页 Request VO")
@Data
public class QcPlanPageReqVO extends PageParam {

    @Schema(description = "检验方案编码")
    private String code;

    @Schema(description = "检验方案名称")
    private String name;

    @Schema(description = "方案版本号", example = "V1.0")
    private String versionNo;

    @Schema(description = "抽样类型", example = "FULL")
    private String sampleType;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
