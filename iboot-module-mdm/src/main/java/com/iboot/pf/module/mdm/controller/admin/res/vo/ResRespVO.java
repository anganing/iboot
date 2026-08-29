package com.iboot.pf.module.mdm.controller.admin.res.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 生产资源 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ResRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32120")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20144")
    @ExcelProperty("工作中心编号")
    private Long wcId;

    @Schema(description = "工作中心编码")
    @ExcelProperty("工作中心编码")
    private String wcCode;

    @Schema(description = "工作中心名称")
    @ExcelProperty("工作中心名称")
    private String wcName;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("资源类型")
    private String resType;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "效率系数")
    @ExcelProperty("效率系数")
    private java.math.BigDecimal efficiency;

    @Schema(description = "可同时承载任务数")
    @ExcelProperty("可同时承载任务数")
    private Integer parallelCapacity;

    @Schema(description = "资源组，同组可互替排程")
    @ExcelProperty("资源组")
    private String resGroup;

    @Schema(description = "默认日历模板编号")
    private Long calendarId;

    @Schema(description = "工厂编号（冗余）")
    private Long plantId;

    @Schema(description = "车间编号（冗余）")
    private Long workshopId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}