package com.iboot.pf.module.wms.controller.admin.invlock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-库存冻结记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InvLockRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14156")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "冻结编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结编号")
    private String lockNo;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源业务类型")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22240")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10149")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4201")
    @ExcelProperty("库位编号")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8353")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15271")
    @ExcelProperty("批次编号")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "13301")
    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "19623")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "25767")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("质量状态")
    private String qualityStatus;

    @Schema(description = "冻结数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结数量，基准单位")
    private BigDecimal qty;

    @Schema(description = "已解冻数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已解冻数量")
    private BigDecimal releasedQty;

    @Schema(description = "冻结原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @ExcelProperty("冻结原因")
    private String reason;

    @Schema(description = "状态：ACTIVE、PARTIAL、RELEASED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：ACTIVE、PARTIAL、RELEASED、CANCELLED")
    private String status;

    @Schema(description = "冻结幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
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

}