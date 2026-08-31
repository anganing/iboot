package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 生产工单分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-生产工单分页 Request VO")
@Data
public class ProdOrderPageReqVO extends PageParam {

    @Schema(description = "生产工单号")
    private String orderNo;

    @Schema(description = "生产需求编号", example = "1")
    private Long demandId;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "生产物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "齐套状态", example = "UNCHECKED")
    private String kitStatus;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "工单交期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dueDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
