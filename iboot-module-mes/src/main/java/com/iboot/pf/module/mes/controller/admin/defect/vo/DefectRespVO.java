package com.iboot.pf.module.mes.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-缺陷记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DefectRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "84")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17410")
    @ExcelProperty("质检任务编号")
    private Long taskId;

    @Schema(description = "关联检验结果编号", example = "23806")
    @ExcelProperty("关联检验结果编号")
    private Long resultId;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("缺陷编码")
    private String defectCode;

    @Schema(description = "缺陷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("缺陷名称")
    private String defectName;

    @Schema(description = "缺陷等级：CRITICAL、MAJOR、MINOR")
    @ExcelProperty("缺陷等级：CRITICAL、MAJOR、MINOR")
    private String defectLevel;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("缺陷数量")
    private BigDecimal qty;

    @Schema(description = "缺陷描述", example = "你猜")
    @ExcelProperty("缺陷描述")
    private String description;

    @Schema(description = "缺陷位置")
    @ExcelProperty("缺陷位置")
    private String location;

    @Schema(description = "缺陷附件地址列表")
    @ExcelProperty("缺陷附件地址列表")
    private String attachmentUrls;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "质检任务task_no")
    @ExcelProperty("质检任务task_no")
    private String taskTask_no;

}