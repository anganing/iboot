package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS-盘点单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CountOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("盘点单号")
    private String countNo;

    @Schema(description = "盘点类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "盘点类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.COUNT_TYPE)
    private String countType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库区编号，0表示全部库区", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("库区编号，0表示全部库区")
    private Long zoneId;

    @Schema(description = "项目编号，0表示不限项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("项目编号，0表示不限项目")
    private Long projectId;

    @Schema(description = "账面快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("账面快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "是否盲盘", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否盲盘")
    private Boolean blindCountFlag;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.ORDER_STATUS)
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "流程审批状态，取值见 BpmProcessInstanceStatusEnum")
    private Integer approvalStatus;

    @Schema(description = "流程实例编号")
    private String processInstanceId;

    @Schema(description = "审批完成时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批意见或驳回原因")
    private String approvalReason;

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
