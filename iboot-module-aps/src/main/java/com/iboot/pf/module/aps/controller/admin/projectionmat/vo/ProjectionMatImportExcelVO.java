package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物料投影 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectionMatImportExcelVO {

    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @ExcelProperty("MDM物料编号")
    private Long sourceMatId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("物料规格快照")
    private String matSpec;

    @ExcelProperty("物料类型快照")
    private String matType;

    @ExcelProperty("基准计量单位编号")
    private Long baseUomId;

    @ExcelProperty("基准计量单位编码快照")
    private String baseUomCode;

    @ExcelProperty("是否批次管控")
    private Boolean batchFlag;

    @ExcelProperty("当前有效BOM编号，0表示无")
    private Long bomId;

    @ExcelProperty("当前有效BOM版本")
    private String bomVersion;

    @ExcelProperty("当前有效工艺路线编号，0表示无")
    private Long routeId;

    @ExcelProperty("当前有效工艺路线版本")
    private String routeVersion;

    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("备注")
    private String remark;

}