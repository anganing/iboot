package com.iboot.pf.module.wms.controller.admin.batchmgr.vo;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;

/**
 * WMS-批次主档批属性维护 Request VO。
 *
 * <p>批次只能由入库动作（到货确认、完工入库）经 {@code WmsBatchService#registerBatch} 创建，
 * 界面上只允许维护"注册之后才知道"的六个批属性。物料、批次号、序列号、供应商这些身份字段
 * 一经注册不可改：库存余额与流水都按 {@code batch_id} 记账，改了身份就与账对不上。
 *
 * <p>字段数本身就是这条约束的证据，不要往里加字段。
 */
@Schema(description = "管理后台 - WMS-批次主档批属性维护 Request VO")
@Data
public class BatchAttrUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "批次编号不能为空")
    private Long id;

    @Schema(description = "炉批号")
    private String furnaceLotNo;

    @Schema(description = "生产日期")
    private LocalDate produceDate;

    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
