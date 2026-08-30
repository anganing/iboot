package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 物料投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionMatRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2416")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10501")
    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @Schema(description = "MDM物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19796")
    @ExcelProperty("MDM物料编号")
    private Long sourceMatId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("物料规格快照")
    private String matSpec;

    @Schema(description = "物料类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("物料类型快照")
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21854")
    @ExcelProperty("基准计量单位编号")
    private Long baseUomId;

    @Schema(description = "基准计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基准计量单位编码快照")
    private String baseUomCode;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否批次管控")
    private Boolean batchFlag;

    @Schema(description = "当前有效BOM编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "22653")
    @ExcelProperty("当前有效BOM编号，0表示无")
    private Long bomId;

    @Schema(description = "当前有效BOM版本")
    @ExcelProperty("当前有效BOM版本")
    private String bomVersion;

    @Schema(description = "当前有效工艺路线编号，0表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "25881")
    @ExcelProperty("当前有效工艺路线编号，0表示无")
    private Long routeId;

    @Schema(description = "当前有效工艺路线版本")
    @ExcelProperty("当前有效工艺路线版本")
    private String routeVersion;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景名称")
    private String scenarioName;

}