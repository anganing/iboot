package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求解快照 Response VO。
 *
 * <p>快照 JSON 可能很大，不进 Excel 导出，只在详情里查看。
 */
@Schema(description = "管理后台 - APS-求解快照 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SolverSnapshotRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "快照类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "INPUT")
    @ExcelProperty(value = "快照类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.SNAPSHOT_TYPE)
    private String snapshotType;

    @Schema(description = "求解输入、结果或约束命中快照，JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String snapshotJson;

    /**
     * 解析后的约束命中对照表。
     *
     * <p>只有 {@code CONSTRAINT_MATCH} 类型的详情接口会填，分页接口一律为空：几十条一起解析没有意义，
     * 列表上也放不下。放在这里而不是让前端解析 {@code snapshotJson}，是因为那份 JSON 里的权重与得分
     * 是 Timefold 的分数字符串，属于求解器细节。
     */
    @Schema(description = "解析后的约束命中对照表，仅约束命中类型的详情有值")
    private SolverSnapshotConstraintMatchRespVO constraintMatch;

    @Schema(description = "快照内容校验值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("校验值")
    private String checksum;

    @Schema(description = "快照生成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("快照生成时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
