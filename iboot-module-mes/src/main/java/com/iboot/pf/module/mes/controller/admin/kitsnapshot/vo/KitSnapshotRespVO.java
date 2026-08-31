package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

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
 * 齐套检查快照 Response VO。
 *
 * <p>快照记录检查那一刻的可用、预留、已领数量，只读，因此没有 SaveReqVO。
 */
@Schema(description = "管理后台 - MES-齐套检查快照 Response VO")
@Data
@ExcelIgnoreUnannotated
public class KitSnapshotRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "齐套检查编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查编号")
    private String checkNo;

    @Schema(description = "检查范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ORDER")
    @ExcelProperty(value = "检查范围", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.KIT_CHECK_SCOPE)
    private String checkScope;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号，0 表示整单检查", example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "工序任务编号，0 表示非工序检查", example = "1")
    private Long opTaskId;

    @Schema(description = "工序任务号")
    @ExcelProperty("工序任务号")
    private String opTaskNo;

    @Schema(description = "检查明细行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @Schema(description = "检查时可用数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("可用数量")
    private BigDecimal availQty;

    @Schema(description = "检查时本需求已预留数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已预留数量")
    private BigDecimal reservedQty;

    @Schema(description = "检查时已领用数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已领用数量")
    private BigDecimal issuedQty;

    @Schema(description = "短缺数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "实际采用替代料编号，0 表示未采用", example = "1")
    private Long altMatId;

    @Schema(description = "替代料名称")
    @ExcelProperty("替代料")
    private String altMatName;

    @Schema(description = "检查结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "FULL_KIT")
    @ExcelProperty(value = "检查结果", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.KIT_STATUS)
    private String result;

    @Schema(description = "检查时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
