package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报工提交请求。创建即提交，界面上没有草稿态的报工。
 *
 * <p>一次报工推进三级数量：工序任务累加，作业工单与生产工单取末道工序的值（不是各工序相加）。
 * 生产工单与作业工单编号由服务端从工序任务反查带出，不由前端提交，避免三者对不上。
 *
 * <p><b>{@code idempotencyKey} 必填，而且服务端不派生 —— 这一点与其它所有动作端点相反。</b>
 * 键格式 {@code UI:{工序任务号}:REPORT:{seq}}，其中 {@code seq} 是"这道工序的第几次报工"，
 * 只有调用方知道。服务端按已有报工数派生的话，每次重试都会算出一个新的 {@code seq}、
 * 每次都建一条新报工记录，幂等当场失效 —— 而报工是所有 MES 动作里最容易被重复提交的一个
 * （扫码枪连扫、弱网重发）。
 *
 * <p>因此对前端有一条硬要求：<b>键要在"这一次报工"的作用域里生成一次并在重试时沿用</b>
 * （例如打开报工弹窗时生成、提交失败重试时不变），而不是每次点击都新生成一个 uuid。
 * 后者与不做幂等等价。
 *
 * <p>三个数量都可留空按零处理，但不能全为零 —— 那条报工没有任何意义。返工数只记在报工记录上、
 * 不进三级数量推进：返工件既没合格也没报废，它还在返工任务里流转，现在就算进产出，返工完再算一次就重复计数了。
 */
@Schema(description = "管理后台 - MES-报工提交 Request VO")
@Data
public class ProdReportSubmitReqVO {

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "幂等键，格式 UI:{工序任务号}:REPORT:{第几次报工}",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "UI:OT-0001:REPORT:1")
    @NotBlank(message = "报工幂等键不能为空，它是重复提交的唯一防线")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "本次合格数，留空按零处理", example = "100")
    @DecimalMin(value = "0", message = "合格数量不能为负数")
    private BigDecimal qtyGood;

    @Schema(description = "本次报废数，留空按零处理", example = "0")
    @DecimalMin(value = "0", message = "报废数量不能为负数")
    private BigDecimal qtyScrap;

    @Schema(description = "本次返工数，留空按零处理；只记录不进数量推进", example = "0")
    @DecimalMin(value = "0", message = "返工数量不能为负数")
    private BigDecimal qtyRework;

    @Schema(description = "报工作业人员编号，留空落 0", example = "1")
    private Long workerId;

    @Schema(description = "生产资源编号，留空落 0", example = "1")
    private Long resId;

    @Schema(description = "班次编号，留空落 0", example = "1")
    private Long shiftId;

    @Schema(description = "本次作业开始时间，留空取报工时间")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间，留空取报工时间，不能早于开始时间")
    private LocalDateTime endTime;

    @Schema(description = "报工时间，留空取当前时间")
    private LocalDateTime reportTime;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
