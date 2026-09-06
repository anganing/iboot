package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 报工取消请求。
 *
 * <p>报工记录是生产事实，<b>不许改也不许删</b>，报错了只能走这个动作冲销：取消会把三级数量反向扣减、
 * 写一条负数量的反向流水，并把记录置为已取消。改删会让进度链失真且事后无从追查，这也是本任务把
 * {@code /update} {@code /delete} {@code /delete-list} 三个端点删掉的理由。
 *
 * <p>已完工工序的报工不允许取消：完工时按"累计合格 + 累计报废 = 计划数"校验过并把工序封版，
 * 抽掉一笔报工会让工序停在"数量不够却已完工"，而完工是终态、没有回退边，自己圆不回来。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{报工单号}:CANCEL_PROD_REPORT:0} 派生
 * ——与提交不同：取消是对一条已存在的报工记录做一次性冲销，单据编号就足以唯一确定这个动作，
 * 派生键反而比前端造键更能拦住连点（重复扣减等于把产量扣成负数）。
 */
@Schema(description = "管理后台 - MES-报工取消 Request VO")
@Data
public class ProdReportCancelReqVO {

    @Schema(description = "报工记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报工记录编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按报工单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "取消原因，写进反向流水；报错数量这类现场失误必须留痕")
    @Size(max = 500, message = "取消原因长度不能超过 500")
    private String reason;

}
