package com.iboot.pf.module.aps.controller.admin.projectionmat.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-MDM物料投影分页 Request VO")
@Data
public class ProjectionMatPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "10501")
    private Long scenarioId;

    @Schema(description = "MDM物料编号", example = "19796")
    private Long sourceMatId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "张三")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "物料类型快照", example = "2")
    private String matType;

    @Schema(description = "基准计量单位编号", example = "21854")
    private Long baseUomId;

    @Schema(description = "基准计量单位编码快照")
    private String baseUomCode;

    @Schema(description = "是否批次管控")
    private Boolean batchFlag;

    @Schema(description = "当前有效BOM编号，0表示无", example = "22653")
    private Long bomId;

    @Schema(description = "当前有效BOM版本")
    private String bomVersion;

    @Schema(description = "当前有效工艺路线编号，0表示无", example = "25881")
    private Long routeId;

    @Schema(description = "当前有效工艺路线版本")
    private String routeVersion;

    @Schema(description = "来源数据更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sourceUpdateTime;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}