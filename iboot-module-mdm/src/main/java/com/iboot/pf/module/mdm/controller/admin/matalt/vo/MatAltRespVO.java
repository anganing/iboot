package com.iboot.pf.module.mdm.controller.admin.matalt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 替代料 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MatAltRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10792")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19327")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "替代物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26792")
    @ExcelProperty("替代物料编号")
    private Long altMatId;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}