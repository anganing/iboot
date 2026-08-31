package com.iboot.pf.module.mes.controller.admin.defect.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 缺陷记录分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-缺陷记录分页 Request VO")
@Data
public class DefectPageReqVO extends PageParam {

    @Schema(description = "质检任务编号", example = "1")
    private Long taskId;

    @Schema(description = "关联检验结果编号", example = "1")
    private Long resultId;

    @Schema(description = "缺陷代码编号", example = "1")
    private Long defectCodeId;

    @Schema(description = "缺陷编码")
    private String defectCode;

    @Schema(description = "缺陷名称")
    private String defectName;

    @Schema(description = "缺陷等级", example = "MAJOR")
    private String defectLevel;

    @Schema(description = "缺陷位置")
    private String location;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
