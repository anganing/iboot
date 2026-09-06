package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领退补料申请创建请求。
 *
 * <p>与 codegen 的 {@link MaterialReqSaveReqVO} 分工明确：这一份走领域 Service，
 * <b>明细留空时按用料关系自动展开</b>——给了工序任务取该工序的路线用料，给了作业工单取该段覆盖的工序用料，
 * 只给生产工单取整单 BOM。展开用的是齐套那一套需求量口径（同一个
 * {@code MesKitRequirementExpander}）：两处各算一遍的话，齐套判齐了而领料少领一行，
 * 车间照样开不了工，而且没人说得清哪个数是对的。倒冲件不进明细（报工时反冲，领它是重复领用），
 * 虚拟件被展开成子件。
 *
 * <p>退料没有用料关系可推（退多少是车间数出来的），必须显式给行，且每行必须指定批次：
 * 退回去的库存要落到原批次上，否则批次台账对不上。
 *
 * <p>单据状态、申请单号、已处理数量、WMS 单号都不由前端提交：状态一律落新建，单号走编码规则，
 * 后两项由确认动作与 WMS 回写。
 */
@Schema(description = "管理后台 - MES-领退补料申请创建 Request VO")
@Data
public class MaterialReqCreateReqVO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，留空或 0 表示工单级申请", example = "0")
    private Long workOrderId;

    @Schema(description = "工序任务编号，留空或 0 表示非工序级申请；给了它就按该工序的用料展开明细", example = "0")
    private Long opTaskId;

    @Schema(description = "申请类型：ISSUE 领料、RETURN 退料、SUPPLEMENT 补料",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "ISSUE")
    @NotEmpty(message = "申请类型不能为空")
    @InEnum(MesMaterialReqTypeEnum.class)
    private String reqType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "领料的来源仓库或退料的目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "申请明细行，留空时按用料关系自动展开；退料必须显式给行")
    @Valid
    private List<Line> lines;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

    @Schema(description = "幂等键，留空由服务端按工单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    /** 申请明细行。 */
    @Schema(description = "领退补料申请明细")
    @Data
    public static class Line {

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "申请数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "申请数量必须大于零")
        private BigDecimal qty;

        @Schema(description = "计量单位编号，留空取物料主档的基本单位", example = "1")
        private Long uomId;

        @Schema(description = "指定批次编号，0 表示不指定批次；退料必填", example = "0")
        private Long batchId;

        @Schema(description = "路线工序用料编号，0 表示不是工序用料", example = "0")
        private Long routeOpMatId;

        @Schema(description = "是否必须开工前发料，留空按是处理", example = "true")
        private Boolean mustIssueBeforeStart;

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过 500")
        private String remark;

    }

}
