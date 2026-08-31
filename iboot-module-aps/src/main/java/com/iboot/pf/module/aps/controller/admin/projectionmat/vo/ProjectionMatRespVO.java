package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物料投影 Response VO。
 *
 * <p>全部字段都是刷新投影时从 mdm 取的快照，场景编码与名称由 Mapper JOIN 取出。
 */
@Schema(description = "管理后台 - APS-物料投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionMatRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long scenarioId;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "MDM 物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceMatId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("规格型号")
    private String matSpec;

    @Schema(description = "物料类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "RAW")
    @ExcelProperty(value = "物料类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.MAT_TYPE)
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long baseUomId;

    @Schema(description = "基准计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基准计量单位")
    private String baseUomCode;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "是否批次管控", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.BOOLEAN_STRING)
    private Boolean batchFlag;

    @Schema(description = "当前有效 BOM 编号，0 表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long bomId;

    @Schema(description = "当前有效 BOM 版本")
    @ExcelProperty("BOM 版本")
    private String bomVersion;

    @Schema(description = "当前有效工艺路线编号，0 表示无", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeId;

    @Schema(description = "当前有效工艺路线版本")
    @ExcelProperty("工艺路线版本")
    private String routeVersion;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
