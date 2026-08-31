package com.iboot.pf.module.wms.controller.admin.containermgr.vo;

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

@Schema(description = "管理后台 - WMS-容器主档 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContainerRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "容器编号或条码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("容器编号或条码")
    private String containerNo;

    @Schema(description = "容器类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("容器类型编号")
    private Long typeId;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "当前仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("当前仓库编号")
    private Long whId;

    @Schema(description = "当前库位编号，0表示未落位", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("当前库位编号，0表示未落位")
    private Long locId;

    @Schema(description = "实际长度，单位毫米")
    @ExcelProperty("实际长度，单位毫米")
    private BigDecimal length;

    @Schema(description = "实际宽度，单位毫米")
    @ExcelProperty("实际宽度，单位毫米")
    private BigDecimal width;

    @Schema(description = "实际高度，单位毫米")
    @ExcelProperty("实际高度，单位毫米")
    private BigDecimal height;

    @Schema(description = "实际毛重")
    @ExcelProperty("实际毛重")
    private BigDecimal grossWeight;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.CONTAINER_STATUS)
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "当前仓库编码")
    @ExcelProperty("当前仓库编码")
    private String whCode;

    @Schema(description = "当前仓库名称")
    @ExcelProperty("当前仓库名称")
    private String whName;

    @Schema(description = "容器类型编码")
    @ExcelProperty("容器类型编码")
    private String typeCode;

    @Schema(description = "容器类型名称")
    @ExcelProperty("容器类型名称")
    private String typeName;

    @Schema(description = "当前库位编码")
    @ExcelProperty("当前库位编码")
    private String locCode;

}
