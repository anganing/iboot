package com.iboot.pf.module.wms.controller.admin.wh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WhRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10993")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8586")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21479")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库编码")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("仓库名称")
    private String name;

    @Schema(description = "仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓")
    private String whType;

    @Schema(description = "管理方式：MANUAL、WCS、AGV、MIXED", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("管理方式：MANUAL、WCS、AGV、MIXED")
    private String managedBy;

    @Schema(description = "外部设备系统编码")
    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}