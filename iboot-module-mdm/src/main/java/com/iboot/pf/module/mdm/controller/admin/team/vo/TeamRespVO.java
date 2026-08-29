package com.iboot.pf.module.mdm.controller.admin.team.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 班组 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TeamRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "装配一班")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "所属车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间编码")
    @ExcelProperty("车间编码")
    private String workshopCode;

    @Schema(description = "车间名称")
    @ExcelProperty("车间名称")
    private String workshopName;

    @Schema(description = "常白/常夜班次编号")
    private Long shiftId;

    @Schema(description = "班次名称")
    @ExcelProperty("班次名称")
    private String shiftName;

    @Schema(description = "班组长用户编号")
    private Long leaderUserId;

    @Schema(description = "班组长姓名")
    @ExcelProperty("班组长")
    private String leaderUserName;

    @Schema(description = "成员数量")
    @ExcelProperty("成员数量")
    private Integer memberCount;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
