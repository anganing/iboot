package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领退补料申请 Response VO。
 *
 * <p>单号来自模块内 JOIN 与快照；库存组织、仓库、项目名称由 Service 调 mdm 与 wms 门面批量补全。
 */
@Schema(description = "管理后台 - MES-领退补料申请 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MaterialReqRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "领退补料申请单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("申请单号")
    private String reqNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "工序任务号")
    @ExcelProperty("工序任务号")
    private String opTaskNo;

    @Schema(description = "申请类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ISSUE")
    @ExcelProperty(value = "申请类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.MATERIAL_REQ_TYPE)
    private String reqType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long whId;

    @Schema(description = "仓库名称")
    @ExcelProperty("仓库")
    private String whName;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "项目名称")
    @ExcelProperty("项目")
    private String projectName;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectTaskId;

    @Schema(description = "WMS 生成的出入库单号")
    @ExcelProperty("WMS 单号")
    private String wmsBizNo;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.MATERIAL_REQ_STATUS)
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
