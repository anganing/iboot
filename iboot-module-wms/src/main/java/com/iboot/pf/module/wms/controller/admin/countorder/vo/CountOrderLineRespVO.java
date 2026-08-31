package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-盘点明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CountOrderLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long balanceId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "库位编号", example = "1")
    private Long locId;

    @Schema(description = "库位编码快照")
    @ExcelProperty("库位")
    private String locCode;

    @Schema(description = "容器编号，0表示散件", example = "0")
    private Long containerId;

    @Schema(description = "容器编号快照")
    @ExcelProperty("容器编号")
    private String containerNo;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @ExcelProperty(value = "质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatus;

    @Schema(description = "项目编号，0表示无项目", example = "0")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "0")
    private Long projectTaskId;

    @Schema(description = "账面数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("账面数量")
    private BigDecimal bookQty;

    @Schema(description = "实盘数量")
    @ExcelProperty("实盘数量")
    private BigDecimal actualQty;

    @Schema(description = "差异数量")
    @ExcelProperty("差异数量")
    private BigDecimal diffQty;

    @Schema(description = "差异调整入出库单号")
    @ExcelProperty("差异调整单号")
    private String adjustBizNo;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "明细状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.COUNT_LINE_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
