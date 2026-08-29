package com.iboot.pf.module.mdm.controller.admin.changeover.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 资源换型矩阵 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ResChangeoverRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "资源编号，0-按资源组生效", example = "1")
    @ExcelProperty("资源编号")
    private Long resId;

    @Schema(description = "资源编码")
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "资源名称")
    @ExcelProperty("资源名称")
    private String resName;

    @Schema(description = "资源组")
    @ExcelProperty("资源组")
    private String resGroup;

    @Schema(description = "换出换型分组", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
    @ExcelProperty("换出分组")
    private String fromGroup;

    @Schema(description = "换入换型分组", requiredMode = Schema.RequiredMode.REQUIRED, example = "蓝色")
    @ExcelProperty("换入分组")
    private String toGroup;

    @Schema(description = "换型分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @ExcelProperty("换型分钟数")
    private Integer changeoverMinutes;

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
