package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

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
 * 检验结果 Response VO。
 *
 * <p>质检任务号来自模块内 JOIN；检验员昵称由 Service 调 system 门面批量补全。
 */
@Schema(description = "管理后台 - MES-检验结果 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcResultRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "质检任务号")
    @ExcelProperty("质检任务号")
    private String taskNo;

    @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("样本序号")
    private Integer sampleNo;

    @Schema(description = "检验项编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项编码")
    private String itemCode;

    @Schema(description = "检验项名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项名称")
    private String itemName;

    @Schema(description = "实测值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("实测值")
    private String actualValue;

    @Schema(description = "是否合格", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @ExcelProperty(value = "是否合格", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BOOLEAN_STRING)
    private Boolean passFlag;

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long inspectorId;

    @Schema(description = "检验员")
    @ExcelProperty("检验员")
    private String inspectorName;

    @Schema(description = "检验时间")
    @ExcelProperty("检验时间")
    private LocalDateTime inspectTime;

    @Schema(description = "本项判定合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "本项判定不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "项目结论", requiredMode = Schema.RequiredMode.REQUIRED, example = "PASS")
    @ExcelProperty(value = "项目结论", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_ITEM_CONCLUSION)
    private String conclusion;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
