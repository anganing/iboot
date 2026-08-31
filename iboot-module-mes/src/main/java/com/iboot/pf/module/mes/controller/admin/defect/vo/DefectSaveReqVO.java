package com.iboot.pf.module.mes.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缺陷记录新增/修改 Request VO。
 *
 * <p>缺陷编码、名称、等级都是 mdm 缺陷代码库的快照，由后端按 defectCodeId 补全，前端只选缺陷代码。
 * 附件是独立子表，走全量替换。
 */
@Schema(description = "管理后台 - MES-缺陷记录新增/修改 Request VO")
@Data
public class DefectSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "关联检验结果编号，0 表示未关联具体检验项", example = "1")
    private Long resultId;

    @Schema(description = "缺陷代码编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺陷代码编号不能为空")
    private Long defectCodeId;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺陷数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "缺陷数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "缺陷描述")
    @Size(max = 500, message = "缺陷描述长度不能超过 500")
    private String description;

    @Schema(description = "缺陷位置")
    @Size(max = 255, message = "缺陷位置长度不能超过 255")
    private String location;

    @Schema(description = "缺陷附件")
    @Valid
    @Size(max = 10, message = "附件数量不能超过 10 个")
    private List<DefectFileReqVO> files;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
