package com.iboot.pf.module.aps.controller.admin.shortage.vo;

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
 * 排程缺料清单 Response VO。
 *
 * <p>物料编码、名称、单位编码是写入时的快照；版本号与场景由 Mapper JOIN 取出，
 * 项目名称由 Service 走 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - APS-排程缺料清单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ShortageRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "生产工单编号，0 表示汇总缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "工序任务编号，0 表示非工序级缺料", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "项目名称")
    @ExcelProperty("项目")
    private String projectName;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectTaskId;

    @Schema(description = "项目任务名称")
    @ExcelProperty("项目任务")
    private String projectTaskName;

    @Schema(description = "短缺物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @Schema(description = "可用数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    @ExcelProperty("可用数量")
    private BigDecimal availQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "需求到位时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求到位时间")
    private LocalDateTime needDate;

    @Schema(description = "预计供给时间")
    @ExcelProperty("预计供给时间")
    private LocalDateTime expectedSupplyDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "OPEN")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.SHORTAGE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
