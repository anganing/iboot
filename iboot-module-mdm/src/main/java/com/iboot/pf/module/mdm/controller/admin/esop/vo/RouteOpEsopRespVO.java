package com.iboot.pf.module.mdm.controller.admin.esop.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 工序 ESOP 作业步骤 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RouteOpEsopRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("路线工序编号")
    private Long routeOpId;

    @Schema(description = "展示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("展示顺序")
    private Integer seq;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "装配要点")
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "作业要点文本")
    private String content;

    @Schema(description = "工艺参数 JSON")
    private String paramJson;

    @Schema(description = "附件列表")
    private List<RouteOpEsopFileRespVO> files;

    @Schema(description = "附件数量")
    @ExcelProperty("附件数量")
    private Integer fileCount;

    @Schema(description = "是否要求作业者确认阅读")
    @ExcelProperty("是否要求确认阅读")
    private Boolean requiredConfirmFlag;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
