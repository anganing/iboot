package com.iboot.pf.module.aps.controller.admin.shortage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - APS-排程缺料清单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ShortageRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24791")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32435")
    @ExcelProperty("排程版本编号")
    private Long versionId;

    @Schema(description = "生产工单编号，0表示汇总缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "22727")
    @ExcelProperty("生产工单编号，0表示汇总缺料")
    private Long prodOrderId;

    @Schema(description = "工序任务编号，0表示非工序级缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "23255")
    @ExcelProperty("工序任务编号，0表示非工序级缺料")
    private Long opTaskId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "22319")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "3778")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "短缺物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8489")
    @ExcelProperty("短缺物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @Schema(description = "可用数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用数量")
    private BigDecimal availQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16732")
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @Schema(description = "需求到位时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求到位时间")
    private LocalDateTime needDate;

    @Schema(description = "预计供给时间")
    @ExcelProperty("预计供给时间")
    private LocalDateTime expectedSupplyDate;

    @Schema(description = "状态：OPEN、PARTIAL、RESOLVED、IGNORED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：OPEN、PARTIAL、RESOLVED、IGNORED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程版本version_no")
    @ExcelProperty("排程版本version_no")
    private String versionVersion_no;

}