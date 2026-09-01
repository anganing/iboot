package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存投影 Response VO。
 *
 * <p>数量与批次是刷新时从 wms 取的快照；场景由 Mapper JOIN 取出，
 * 物料名称与项目名称由 Service 走门面批量补全（投影表只落了物料编码）。
 */
@Schema(description = "管理后台 - APS-库存投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionInvRespVO {

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

    @Schema(description = "WMS 库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceBalanceId;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long invOrgId;

    @Schema(description = "库存组织名称")
    @ExcelProperty("库存组织")
    private String invOrgName;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long whId;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库")
    private String whName;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long locId;

    @Schema(description = "库位编码")
    @ExcelProperty("库位")
    private String locName;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "批次编号，0 表示无批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long batchId;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "项目名称")
    @ExcelProperty("项目")
    private String projectName;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectTaskId;

    @Schema(description = "库存范围快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "COMMON")
    @ExcelProperty(value = "库存范围", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.STOCK_SCOPE)
    private String stockScope;

    @Schema(description = "质量状态快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @ExcelProperty(value = "质量状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatus;

    @Schema(description = "现存数量快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("现存数量")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("预留数量")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("冻结数量")
    private BigDecimal qtyFrozen;

    @Schema(description = "可用数量快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("可用数量")
    private BigDecimal qtyAvailable;

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
