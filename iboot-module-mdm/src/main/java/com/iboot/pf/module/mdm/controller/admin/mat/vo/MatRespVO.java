package com.iboot.pf.module.mdm.controller.admin.mat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 物料主档 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MatRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29413")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "物料分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8083")
    @ExcelProperty("物料分类编号")
    private Long catId;

    @Schema(description = "物料分类编码")
    @ExcelProperty("物料分类编码")
    private String catCode;

    @Schema(description = "物料分类名称")
    @ExcelProperty("物料分类名称")
    private String catName;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "规格型号")
    @ExcelProperty("规格型号")
    private String spec;

    @Schema(description = "图号")
    @ExcelProperty("图号")
    private String drawingNo;

    @Schema(description = "物料类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("物料类型")
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "35")
    @ExcelProperty("基准计量单位编号")
    private Long baseUomId;

    @Schema(description = "基准计量单位编码")
    @ExcelProperty("基准计量单位编码")
    private String baseUomCode;

    @Schema(description = "基准计量单位名称")
    @ExcelProperty("基准计量单位名称")
    private String baseUomName;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否批次管控")
    private Boolean batchFlag;

    @Schema(description = "是否SN管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否SN管控")
    private Boolean snFlag;

    @Schema(description = "保质期（天）")
    @ExcelProperty("保质期（天）")
    private Integer shelfLifeDays;

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