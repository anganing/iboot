package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 排程场景 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ScenarioRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3976")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程场景编码")
    private String code;

    @Schema(description = "排程场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("排程场景名称")
    private String name;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20450")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号，0表示全工厂", requiredMode = Schema.RequiredMode.REQUIRED, example = "14740")
    @ExcelProperty("车间编号，0表示全工厂")
    private Long workshopId;

    @Schema(description = "排程策略编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18129")
    @ExcelProperty("排程策略编号")
    private Long strategyId;

    @Schema(description = "计划窗口开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划窗口开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划窗口结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划窗口结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "输入数据快照时间")
    @ExcelProperty("输入数据快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}