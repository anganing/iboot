package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

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
 * 生产工单 Response VO。
 *
 * <p>需求编号来自模块内 JOIN；工厂、车间、项目名称由 Service 调 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-生产工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "生产工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号")
    private String orderNo;

    @Schema(description = "生产需求编号", example = "1")
    private Long demandId;

    @Schema(description = "生产需求单号")
    @ExcelProperty("生产需求单号")
    private String demandNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间名称")
    @ExcelProperty("车间")
    private String workshopName;

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

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
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

    @Schema(description = "计划生产数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("计划数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "累计报废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "锁定的 BOM 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long bomId;

    @Schema(description = "锁定的 BOM 版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("BOM 版本")
    private String bomVersion;

    @Schema(description = "锁定的工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeId;

    @Schema(description = "锁定的工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工艺路线版本")
    private String routeVersion;

    @Schema(description = "工单交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工单交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "齐套状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "UNCHECKED")
    @ExcelProperty(value = "齐套状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.KIT_STATUS)
    private String kitStatus;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.PROD_ORDER_STATUS)
    private String status;

    @Schema(description = "下达时间")
    @ExcelProperty("下达时间")
    private LocalDateTime releaseTime;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
