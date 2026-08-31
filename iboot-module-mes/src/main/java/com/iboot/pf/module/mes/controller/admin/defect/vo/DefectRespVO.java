package com.iboot.pf.module.mes.controller.admin.defect.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 缺陷记录 Response VO。
 *
 * <p>列表只带附件数量，详情才带附件明细，避免分页记录数被 JOIN 放大。
 */
@Schema(description = "管理后台 - MES-缺陷记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DefectRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "质检任务号")
    @ExcelProperty("质检任务号")
    private String taskNo;

    @Schema(description = "关联检验结果编号", example = "1")
    private Long resultId;

    @Schema(description = "关联检验项名称")
    @ExcelProperty("检验项名称")
    private String resultItemName;

    @Schema(description = "缺陷代码编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long defectCodeId;

    @Schema(description = "缺陷编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("缺陷编码")
    private String defectCode;

    @Schema(description = "缺陷名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("缺陷名称")
    private String defectName;

    @Schema(description = "缺陷等级快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "MAJOR")
    @ExcelProperty(value = "缺陷等级", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DEFECT_LEVEL)
    private String defectLevel;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("缺陷数量")
    private BigDecimal qty;

    @Schema(description = "缺陷描述")
    @ExcelProperty("缺陷描述")
    private String description;

    @Schema(description = "缺陷位置")
    @ExcelProperty("缺陷位置")
    private String location;

    @Schema(description = "附件数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("附件数量")
    private Integer fileCount;

    @Schema(description = "缺陷附件明细，仅详情返回")
    private List<DefectFileRespVO> files;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
