package com.iboot.pf.module.wms.controller.admin.invreservation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;

@Schema(description = "管理后台 - WMS-库存预留 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InvReservationRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "预留编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预留编号")
    private String reservationNo;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "来源业务类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.SOURCE_BIZ_TYPE)
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务行号")
    @ExcelProperty("来源业务行号")
    private String bizLineNo;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库位编号")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("批次编号")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatus;

    @Schema(description = "预留数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预留数量，基准单位")
    private BigDecimal qty;

    @Schema(description = "已释放或已核销数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已释放或已核销数量")
    private BigDecimal releasedQty;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.RESERVATION_STATUS)
    private String status;

    @Schema(description = "预留失效时间")
    @ExcelProperty("预留失效时间")
    private LocalDateTime expireTime;

    @Schema(description = "预留幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预留幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "仓库编码")
    @ExcelProperty("仓库编码")
    private String whCode;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库名称")
    private String whName;

    @Schema(description = "库位编码")
    @ExcelProperty("库位编码")
    private String locCode;

    @Schema(description = "批次号")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    @ExcelProperty("物料名称")
    private String matName;

}
