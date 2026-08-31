package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在制流转流水 Response VO。
 *
 * <p>流水是过账留痕的证据链，只读，因此没有 SaveReqVO。
 */
@Schema(description = "管理后台 - MES-在制流转流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WipTxnRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "在制事务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务编号")
    private String txnNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "工序任务号")
    @ExcelProperty("工序任务号")
    private String opTaskNo;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "事务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "START")
    @ExcelProperty(value = "事务类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.WIP_TXN_TYPE)
    private String txnType;

    @Schema(description = "事务数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("事务数量")
    private BigDecimal qty;

    @Schema(description = "变更前状态", example = "READY")
    @ExcelProperty(value = "变更前状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.OP_TASK_STATUS)
    private String fromStatus;

    @Schema(description = "变更后状态", example = "RUNNING")
    @ExcelProperty(value = "变更后状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.OP_TASK_STATUS)
    private String toStatus;

    @Schema(description = "来源业务类型", example = "PROD_REPORT")
    @ExcelProperty(value = "来源业务类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BIZ_TYPE)
    private String bizType;

    @Schema(description = "来源业务单号")
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "来源工序任务编号，0 表示无", example = "1")
    private Long fromOpTaskId;

    @Schema(description = "来源工序任务号")
    @ExcelProperty("来源工序任务号")
    private String fromOpTaskNo;

    @Schema(description = "目标工序任务编号，0 表示无", example = "1")
    private Long toOpTaskId;

    @Schema(description = "目标工序任务号")
    @ExcelProperty("目标工序任务号")
    private String toOpTaskNo;

    @Schema(description = "事务发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("事务发生时间")
    private LocalDateTime txnTime;

    @Schema(description = "事务幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
