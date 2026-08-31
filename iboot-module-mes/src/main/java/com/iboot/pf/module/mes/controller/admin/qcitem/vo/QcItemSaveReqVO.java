package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检验项目新增/修改 Request VO。
 *
 * <p>检验项是检验方案的子表，父键 planId 由前端从当前方案带入。数值型检验项必须给上限或下限，
 * 且上限不能小于下限，这些跨字段规则在 Service 里校验。
 */
@Schema(description = "管理后台 - MES-检验项目新增/修改 Request VO")
@Data
public class QcItemSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验方案编号不能为空")
    private Long planId;

    @Schema(description = "检验项编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验项编码不能为空")
    @Size(max = 64, message = "检验项编码长度不能超过 64")
    private String itemCode;

    @Schema(description = "检验项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验项名称不能为空")
    @Size(max = 128, message = "检验项名称长度不能超过 128")
    private String itemName;

    @Schema(description = "检验项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "NUMERIC")
    @NotEmpty(message = "检验项类型不能为空")
    @InEnum(MesQcItemTypeEnum.class)
    private String itemType;

    @Schema(description = "检验值单位")
    @Size(max = 32, message = "检验值单位长度不能超过 32")
    private String unit;

    @Schema(description = "标准值")
    @Size(max = 255, message = "标准值长度不能超过 255")
    private String stdValue;

    @Schema(description = "数值上限", example = "1")
    private BigDecimal upperLimit;

    @Schema(description = "数值下限", example = "1")
    private BigDecimal lowerLimit;

    @Schema(description = "是否必检", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否必检不能为空")
    private Boolean requiredFlag;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于零")
    private Integer sortNo;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
