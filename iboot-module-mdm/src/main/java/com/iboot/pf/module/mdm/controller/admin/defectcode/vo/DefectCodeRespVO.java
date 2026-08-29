package com.iboot.pf.module.mdm.controller.admin.defectcode.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 缺陷代码 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DefectCodeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SCRATCH")
    @ExcelProperty("缺陷编码")
    private String code;

    @Schema(description = "缺陷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "表面划伤")
    @ExcelProperty("缺陷名称")
    private String name;

    @Schema(description = "缺陷等级：CRITICAL-致命，MAJOR-严重，MINOR-轻微",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "MAJOR")
    @ExcelProperty("缺陷等级")
    private String level;

    @Schema(description = "适用工序编号列表，为空表示不限")
    private List<Long> opIds;

    @Schema(description = "适用物料分类编号列表，为空表示不限")
    private List<Long> matCatIds;

    @Schema(description = "适用工序编码，逗号分隔；为空表示不限")
    @ExcelProperty("适用工序")
    private String opNames;

    @Schema(description = "适用物料分类名称，逗号分隔；为空表示不限")
    @ExcelProperty("适用物料分类")
    private String matCatNames;

    @Schema(description = "缺陷分类树父级编号，0-顶级", example = "0")
    @ExcelProperty("父级编号")
    private Long parentId;

    @Schema(description = "父级缺陷名称")
    @ExcelProperty("父级缺陷名称")
    private String parentName;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
