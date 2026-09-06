package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 生产工单下达请求。
 *
 * <p>与 {@link ProdOrderActionReqVO} 分开是因为下达落的是备注而不是原因：领域侧
 * {@code MesReleaseOrderBO} 的字段名就是 {@code remark}，而挂起、恢复、取消三个动作要记的是
 * "为什么这么做"，落 {@code reason}。
 *
 * <p><b>刻意没有 BOM 版本、工艺版本与生效日期入参</b>：下达锁的必须是"下达那一刻"的生效版本，
 * 允许调用方指定就等于允许锁一个不生效的版本，"在制工单按下达时的版本执行"这块基石当场失效。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{工单号}:RELEASE_PROD_ORDER:0} 派生。
 * 下达会展开一整批作业工单与工序任务，键不稳定就会展开两批。
 */
@Schema(description = "管理后台 - MES-生产工单下达 Request VO")
@Data
public class ProdOrderReleaseReqVO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按工单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "下达备注，写进在制流水")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
