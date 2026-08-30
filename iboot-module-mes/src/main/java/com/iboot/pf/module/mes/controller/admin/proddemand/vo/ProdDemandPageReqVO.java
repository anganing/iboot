package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-生产需求分页 Request VO")
@Data
public class ProdDemandPageReqVO extends PageParam {

    @Schema(description = "生产需求编号")
    private String demandNo;

    @Schema(description = "来源类型：MANUAL、EXCEL、ERP、APS", example = "2")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", example = "7793")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", example = "19484")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "28313")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", example = "10796")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "芋艿")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "需求数量")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "3052")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    private String uomCode;

    @Schema(description = "需求交期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dueDate;

    @Schema(description = "优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED", example = "1")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}