package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-质检任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcTaskImportExcelVO {

    @ExcelProperty("质检任务号")
    private String taskNo;

    @ExcelProperty("命中的质检规则编号")
    private Long ruleId;

    @ExcelProperty("锁定的检验方案编号")
    private Long qcPlanId;

    @ExcelProperty("锁定的检验方案版本")
    private String qcPlanVersion;

    @ExcelProperty("触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @ExcelProperty("来源业务类型")
    private String bizType;

    @ExcelProperty("来源业务单号")
    private String bizNo;

    @ExcelProperty("来源业务明细编号")
    private Long bizLineId;

    @ExcelProperty("检验物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("物料规格快照")
    private String matSpec;

    @ExcelProperty("检验批次编号")
    private Long batchId;

    @ExcelProperty("批次号快照")
    private String lotNo;

    @ExcelProperty("送检数量")
    private BigDecimal qty;

    @ExcelProperty("抽样数量")
    private BigDecimal sampleQty;

    @ExcelProperty("计量单位编号")
    private Long uomId;

    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @ExcelProperty("指派检验员编号")
    private Long assignedInspectorId;

    @ExcelProperty("检验开始时间")
    private LocalDateTime inspectStartTime;

    @ExcelProperty("检验结束时间")
    private LocalDateTime inspectEndTime;

    @ExcelProperty("检验结论：QUALIFIED、REJECTED、CONCESSION")
    private String conclusion;

    @ExcelProperty("合格数量")
    private BigDecimal qtyQualified;

    @ExcelProperty("不合格数量")
    private BigDecimal qtyRejected;

    @ExcelProperty("状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}