package com.iboot.pf.module.wms.controller.admin.wave.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-出库波次 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WaveRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30246")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "波次编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("波次编号")
    private String waveNo;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9063")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "波次类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("波次类型")
    private String waveType;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "仓库编码")
    @ExcelProperty("仓库编码")
    private String whCode;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库名称")
    private String whName;

}