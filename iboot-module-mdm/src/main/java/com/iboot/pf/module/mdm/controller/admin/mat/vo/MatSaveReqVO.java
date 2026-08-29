package com.iboot.pf.module.mdm.controller.admin.mat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 物料主档新增/修改 Request VO")
@Data
public class MatSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29413")
    private Long id;

    @Schema(description = "物料分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8083")
    @NotNull(message = "物料分类编号不能为空")
    private Long catId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "规格型号")
    private String spec;

    @Schema(description = "图号")
    private String drawingNo;

    @Schema(description = "物料类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "物料类型不能为空")
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "35")
    @NotNull(message = "基准计量单位编号不能为空")
    private Long baseUomId;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否批次管控不能为空")
    private Boolean batchFlag;

    @Schema(description = "是否SN管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否SN管控不能为空")
    private Boolean snFlag;

    @Schema(description = "保质期（天）")
    private Integer shelfLifeDays;

    // ========== 储运属性（尺寸单位毫米，体积立方毫米） ==========

    @Schema(description = "净重")
    @DecimalMin(value = "0", message = "净重不能小于 0")
    private BigDecimal netWeight;

    @Schema(description = "毛重")
    @DecimalMin(value = "0", message = "毛重不能小于 0")
    private BigDecimal grossWeight;

    @Schema(description = "重量单位编号")
    private Long weightUomId;

    @Schema(description = "长（毫米）")
    @DecimalMin(value = "0", message = "长不能小于 0")
    private BigDecimal length;

    @Schema(description = "宽（毫米）")
    @DecimalMin(value = "0", message = "宽不能小于 0")
    private BigDecimal width;

    @Schema(description = "高（毫米）")
    @DecimalMin(value = "0", message = "高不能小于 0")
    private BigDecimal height;

    @Schema(description = "体积（立方毫米）")
    @DecimalMin(value = "0", message = "体积不能小于 0")
    private BigDecimal volume;

    // ========== 计划参数 ==========

    @Schema(description = "计划策略：MTO-按单，MTS-备库")
    @Pattern(regexp = "MTO|MTS", message = "计划策略只能是 MTO 或 MTS")
    private String planStrategy;

    @Schema(description = "生产/采购提前期（天）")
    @Min(value = 0, message = "提前期不能小于 0")
    private Integer leadTimeDays;

    @Schema(description = "安全库存")
    @DecimalMin(value = "0", message = "安全库存不能小于 0")
    private BigDecimal safetyStock;

    @Schema(description = "最小订货量")
    @DecimalMin(value = "0", message = "最小订货量不能小于 0")
    private BigDecimal minOrderQty;

    @Schema(description = "订货倍数")
    @DecimalMin(value = "0", message = "订货倍数不能小于 0")
    private BigDecimal orderMultiple;

    // ========== 仓储默认值 ==========

    @Schema(description = "默认仓库编号")
    private Long defaultWhId;

    @Schema(description = "默认库位编号")
    private Long defaultLocId;

    @Schema(description = "标准包装量")
    @DecimalMin(value = "0", message = "标准包装量不能小于 0")
    private BigDecimal packQty;

    @Schema(description = "ABC分类")
    @Pattern(regexp = "A|B|C", message = "ABC分类只能是 A、B 或 C")
    private String abcClass;

    // ========== 换型 ==========

    @Schema(description = "换型分组")
    @Size(max = 64, message = "换型分组长度不能超过 64")
    private String changeoverGroup;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}