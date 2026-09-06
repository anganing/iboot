package com.iboot.pf.module.mes.controller.admin.optask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工序任务完工请求。
 *
 * <p>两个数量是<b>可选的对账声明，不是数量入口</b>：合格数与报废数只能由报工推进，完工只校验累计数量凑齐了
 * （累计合格 + 累计报废 = 计划数）并把工序封版。给了就要求与工序任务上的累计值相等，
 * 这道校验防的是界面数据过期 —— 工人看到 100/0 就点了完工，而后台已被另一笔报工改成 90/10，
 * 此时完工的是一个他没看见的数量。两个都留空时只校验累计值本身。
 */
@Schema(description = "管理后台 - MES-工序任务完工 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpTaskFinishReqVO extends OpTaskActionReqVO {

    @Schema(description = "界面上看到的累计合格数，可空；给了就必须与工序任务上的累计值相等", example = "100")
    @DecimalMin(value = "0", message = "累计合格数不能为负数")
    private BigDecimal qualifiedQty;

    @Schema(description = "界面上看到的累计报废数，可空；给了就必须与工序任务上的累计值相等", example = "0")
    @DecimalMin(value = "0", message = "累计报废数不能为负数")
    private BigDecimal scrapQty;

}
