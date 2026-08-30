package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-检验方案 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcPlanRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13085")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "检验方案编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验方案编码")
    private String code;

    @Schema(description = "检验方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("检验方案名称")
    private String name;

    @Schema(description = "方案版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("方案版本号")
    private String versionNo;

    @Schema(description = "抽样类型：FULL、FIXED_QTY、PERCENT、AQL", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("抽样类型：FULL、FIXED_QTY、PERCENT、AQL")
    private String sampleType;

    @Schema(description = "固定数量或抽样比例")
    @ExcelProperty("固定数量或抽样比例")
    private BigDecimal sampleValue;

    @Schema(description = "AQL接收质量限")
    @ExcelProperty("AQL接收质量限")
    private String aql;

    @Schema(description = "接收判定规则")
    @ExcelProperty("接收判定规则")
    private String acceptanceRule;

    @Schema(description = "状态：DRAFT、PUBLISHED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、PUBLISHED、DISABLED")
    private String status;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}