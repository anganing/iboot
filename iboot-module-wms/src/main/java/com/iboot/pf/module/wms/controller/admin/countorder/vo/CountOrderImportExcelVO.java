package com.iboot.pf.module.wms.controller.admin.countorder.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-盘点单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountOrderImportExcelVO {

    @ExcelProperty("盘点单号")
    private String countNo;

    @ExcelProperty("盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC")
    private String countType;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("库区编号，0表示全部库区")
    private Long zoneId;

    @ExcelProperty("项目编号，0表示不限项目")
    private Long projectId;

    @ExcelProperty("账面快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("是否盲盘")
    private Boolean blindCountFlag;

    @ExcelProperty("状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}