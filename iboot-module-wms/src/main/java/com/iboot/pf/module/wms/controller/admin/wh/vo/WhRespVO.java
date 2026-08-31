package com.iboot.pf.module.wms.controller.admin.wh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import com.iboot.pf.module.system.enums.DictTypeConstants;

@Schema(description = "管理后台 - WMS-仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WhRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库编码")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库名称")
    private String name;

    @Schema(description = "仓库类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "仓库类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.WH_TYPE)
    private String whType;

    @Schema(description = "管理方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "管理方式", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.MANAGED_BY)
    private String managedBy;

    @Schema(description = "外部设备系统编码")
    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "库存组织编码")
    @ExcelProperty("库存组织编码")
    private String invOrgCode;

    @Schema(description = "库存组织名称")
    @ExcelProperty("库存组织名称")
    private String invOrgName;

    @Schema(description = "工厂编码")
    @ExcelProperty("工厂编码")
    private String plantCode;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂名称")
    private String plantName;

}
