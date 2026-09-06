package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产工单关闭请求。
 *
 * <p>关闭是账目收尾动作，除了状态推进还要释放剩余预留、给倒冲件按实际产量反冲扣料、回写需求状态。
 * 因此比其他动作多两个仓储维度，与领域侧 {@code MesCloseOrderBO} 的字段一一对应。
 *
 * <p>两个字段只服务反冲那一步：倒冲件从来不预留（齐套与领料都把它剔除），没有"预留占住的库位"可以回落，
 * 所以要么这里指定，要么回落到倒冲物料自己的默认仓库与默认库位。留空是常态，
 * 只有默认仓库不一致（服务端会报错并列出物料编码）时才需要显式指定。
 */
@Schema(description = "管理后台 - MES-生产工单关闭 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProdOrderCloseReqVO extends ProdOrderActionReqVO {

    @Schema(description = "倒冲反冲的发料仓库编号，留空回落倒冲物料的默认仓库", example = "1")
    private Long whId;

    @Schema(description = "倒冲反冲的发料库位编号，留空回落倒冲物料的默认库位", example = "1")
    private Long locId;

}
