package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 求解快照 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolverSnapshotImportExcelVO {

    @ExcelProperty("排程版本编号")
    private Long versionId;

    @ExcelProperty("快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR")
    private String snapshotType;

    @ExcelProperty("Timefold求解输入、结果或约束命中快照")
    private String snapshotJson;

    @ExcelProperty("快照内容校验值")
    private String checksum;

    @ExcelProperty("快照生成时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("备注")
    private String remark;

}