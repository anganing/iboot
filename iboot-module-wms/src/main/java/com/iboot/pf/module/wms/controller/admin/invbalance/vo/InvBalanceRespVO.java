package com.iboot.pf.module.wms.controller.admin.invbalance.vo;

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

@Schema(description = "管理后台 - WMS-多维库存余额 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InvBalanceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库位编号")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "批次编号；无批次物料挂默认批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("批次编号；无批次物料挂默认批次")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目归属", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("项目编号，0表示无项目归属")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务归属", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("项目任务编号，0表示无项目任务归属")
    private Long projectTaskId;

    @Schema(description = "库存范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "库存范围", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.STOCK_SCOPE)
    private String stockScope;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "质量状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.QUALITY_STATUS)
    private String qualityStatus;

    @Schema(description = "现存数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("现存数量，基准单位")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预留数量，基准单位")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结数量，基准单位")
    private BigDecimal qtyFrozen;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("物料规格快照")
    private String matSpec;

    @Schema(description = "基准单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("基准单位编码快照")
    private String uomCode;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号快照")
    private String lotNo;

    @Schema(description = "仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库编码快照")
    private String whCode;

    @Schema(description = "库位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库位编码快照")
    private String locCode;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "仓库名称")
    @ExcelProperty("仓库名称")
    private String whName;

    @Schema(description = "归属项目名称")
    @ExcelProperty("归属项目名称")
    private String projectName;

}
