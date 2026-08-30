package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 求解快照 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SolverSnapshotRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2308")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26936")
    @ExcelProperty("排程版本编号")
    private Long versionId;

    @Schema(description = "快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR")
    private String snapshotType;

    @Schema(description = "Timefold求解输入、结果或约束命中快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Timefold求解输入、结果或约束命中快照")
    private String snapshotJson;

    @Schema(description = "快照内容校验值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("快照内容校验值")
    private String checksum;

    @Schema(description = "快照生成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("快照生成时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private String versionVersion_no;

}