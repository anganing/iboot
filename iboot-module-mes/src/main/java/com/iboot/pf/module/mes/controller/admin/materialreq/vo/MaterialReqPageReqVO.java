package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 领退补料申请分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-领退补料申请分页 Request VO")
@Data
public class MaterialReqPageReqVO extends PageParam {

    @Schema(description = "申请单号")
    private String reqNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "申请类型", example = "ISSUE")
    private String reqType;

    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "WMS 单号")
    private String wmsBizNo;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
