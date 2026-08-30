package com.iboot.pf.module.aps.controller.admin.shortage.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-排程缺料清单分页 Request VO")
@Data
public class ShortagePageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "32435")
    private Long versionId;

    @Schema(description = "生产工单编号，0表示汇总缺料", example = "22727")
    private Long prodOrderId;

    @Schema(description = "工序任务编号，0表示非工序级缺料", example = "23255")
    private Long opTaskId;

    @Schema(description = "项目编号，0表示无项目", example = "22319")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "3778")
    private Long projectTaskId;

    @Schema(description = "短缺物料编号", example = "8489")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "张三")
    private String matName;

    @Schema(description = "需求数量")
    private BigDecimal reqQty;

    @Schema(description = "可用数量")
    private BigDecimal availQty;

    @Schema(description = "短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "计量单位编号", example = "16732")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    private String uomCode;

    @Schema(description = "需求到位时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] needDate;

    @Schema(description = "预计供给时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expectedSupplyDate;

    @Schema(description = "状态：OPEN、PARTIAL、RESOLVED、IGNORED", example = "1")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}