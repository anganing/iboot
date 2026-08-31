package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产需求 Response VO。
 *
 * <p>物料、单位相关字段是写入时落库的快照；工厂、项目名称由 Service 层调用 mdm 门面批量补全，
 * 不在 Mapper 里跨库 JOIN。
 */
@Schema(description = "管理后台 - MES-生产需求 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdDemandRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产需求编号")
    private String demandNo;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MANUAL")
    @ExcelProperty(value = "来源类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DEMAND_SOURCE_TYPE)
    private String sourceType;

    @Schema(description = "来源单号")
    @ExcelProperty("来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

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

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("规格型号")
    private String matSpec;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("需求数量")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "需求交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("需求交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DEMAND_STATUS)
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
