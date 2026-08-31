package com.iboot.pf.module.wms.controller.admin.invtxn.vo;

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

@Schema(description = "管理后台 - WMS-库存流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InvTxnRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库存事务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库存事务编号")
    private String txnNo;

    @Schema(description = "事务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "事务类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.TXN_TYPE)
    private String txnType;

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

    @Schema(description = "收发方向", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "收发方向", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.IO_TYPE)
    private String ioType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "来源库位编号，0表示无来源库位", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("来源库位编号，0表示无来源库位")
    private Long fromLocId;

    @Schema(description = "目标库位编号，0表示无目标库位", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("目标库位编号，0表示无目标库位")
    private Long toLocId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("批次编号")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("基准计量单位编号")
    private Long uomId;

    @Schema(description = "事务数量，使用正数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务数量，使用正数")
    private BigDecimal qty;

    @Schema(description = "过账前现存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("过账前现存数量")
    private BigDecimal beforeQty;

    @Schema(description = "过账后现存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("过账后现存数量")
    private BigDecimal afterQty;

    @Schema(description = "变更前质量状态")
    @ExcelProperty(value = "变更前质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatusFrom;

    @Schema(description = "变更后质量状态")
    @ExcelProperty(value = "变更后质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatusTo;

    @Schema(description = "变更前项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更前项目编号，0表示无项目")
    private Long projectIdFrom;

    @Schema(description = "变更前项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更前项目任务编号，0表示无项目任务")
    private Long projectTaskIdFrom;

    @Schema(description = "变更后项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更后项目编号，0表示无项目")
    private Long projectIdTo;

    @Schema(description = "变更后项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更后项目任务编号，0表示无项目任务")
    private Long projectTaskIdTo;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单位编码快照")
    private String uomCode;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号快照")
    private String lotNo;

    @Schema(description = "来源库位编码快照")
    @ExcelProperty("来源库位编码快照")
    private String fromLocCode;

    @Schema(description = "目标库位编码快照")
    @ExcelProperty("目标库位编码快照")
    private String toLocCode;

    @Schema(description = "过账幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("过账幂等键")
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

}
