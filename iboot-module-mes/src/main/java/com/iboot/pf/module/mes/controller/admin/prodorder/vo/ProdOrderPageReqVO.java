package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-生产工单分页 Request VO")
@Data
public class ProdOrderPageReqVO extends PageParam {

    @Schema(description = "生产工单号")
    private String orderNo;

    @Schema(description = "生产需求编号", example = "32290")
    private Long demandId;

    @Schema(description = "工厂编号", example = "8133")
    private Long plantId;

    @Schema(description = "车间编号", example = "26390")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", example = "24435")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "4361")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", example = "20684")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "王五")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "计划生产数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "累计报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "计量单位编号", example = "26955")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    private String uomCode;

    @Schema(description = "锁定的BOM编号", example = "30169")
    private Long bomId;

    @Schema(description = "锁定的BOM版本")
    private String bomVersion;

    @Schema(description = "锁定的工艺路线编号", example = "32358")
    private Long routeId;

    @Schema(description = "锁定的工艺路线版本")
    private String routeVersion;

    @Schema(description = "工单交期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dueDate;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE", example = "1")
    private String kitStatus;

    @Schema(description = "状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED", example = "2")
    private String status;

    @Schema(description = "下达时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] releaseTime;

    @Schema(description = "实际开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualStartTime;

    @Schema(description = "实际结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] actualEndTime;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}