package com.iboot.pf.module.mes.controller.admin.defect.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-缺陷记录分页 Request VO")
@Data
public class DefectPageReqVO extends PageParam {

    @Schema(description = "质检任务编号", example = "17410")
    private Long taskId;

    @Schema(description = "关联检验结果编号", example = "23806")
    private Long resultId;

    @Schema(description = "缺陷编码")
    private String defectCode;

    @Schema(description = "缺陷名称", example = "赵六")
    private String defectName;

    @Schema(description = "缺陷等级：CRITICAL、MAJOR、MINOR")
    private String defectLevel;

    @Schema(description = "缺陷数量")
    private BigDecimal qty;

    @Schema(description = "缺陷描述", example = "你猜")
    private String description;

    @Schema(description = "缺陷位置")
    private String location;

    @Schema(description = "缺陷附件地址列表")
    private String attachmentUrls;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}