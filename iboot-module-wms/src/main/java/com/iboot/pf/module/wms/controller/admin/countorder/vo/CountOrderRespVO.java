package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-盘点单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CountOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19625")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("盘点单号")
    private String countNo;

    @Schema(description = "盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC")
    private String countType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21976")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6193")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库区编号，0表示全部库区", requiredMode = Schema.RequiredMode.REQUIRED, example = "23296")
    @ExcelProperty("库区编号，0表示全部库区")
    private Long zoneId;

    @Schema(description = "项目编号，0表示不限项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "19244")
    @ExcelProperty("项目编号，0表示不限项目")
    private Long projectId;

    @Schema(description = "账面快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("账面快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "是否盲盘", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否盲盘")
    private Boolean blindCountFlag;

    @Schema(description = "状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
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

    @Schema(description = "库区编号，0表示全部库区编码")
    @ExcelProperty("库区编号，0表示全部库区编码")
    private String zoneCode;

    @Schema(description = "库区编号，0表示全部库区名称")
    @ExcelProperty("库区编号，0表示全部库区名称")
    private String zoneName;

}