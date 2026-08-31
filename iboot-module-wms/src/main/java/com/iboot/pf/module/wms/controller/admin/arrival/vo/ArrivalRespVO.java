package com.iboot.pf.module.wms.controller.admin.arrival.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;

@Schema(description = "管理后台 - WMS-到货登记 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ArrivalRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "到货登记单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("到货登记单号")
    private String arrivalNo;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "来源类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ARRIVAL_SOURCE_TYPE)
    private String sourceType;

    @Schema(description = "来源单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源单号")
    private String sourceNo;

    @Schema(description = "供应商编号", example = "1")
    @ExcelProperty("供应商编号")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    @ExcelProperty("供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商名称快照")
    @ExcelProperty("供应商名称快照")
    private String supplierName;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("目标仓库编号")
    private Long whId;

    @Schema(description = "预计到货日期")
    @ExcelProperty("预计到货日期")
    private LocalDate expectDate;

    @Schema(description = "实际到货时间")
    @ExcelProperty("实际到货时间")
    private LocalDateTime arrivalTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ARRIVAL_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "目标仓库编码")
    @ExcelProperty("目标仓库编码")
    private String whCode;

    @Schema(description = "目标仓库名称")
    @ExcelProperty("目标仓库名称")
    private String whName;

}
