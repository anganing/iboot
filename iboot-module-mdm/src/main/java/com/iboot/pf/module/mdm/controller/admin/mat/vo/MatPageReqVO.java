package com.iboot.pf.module.mdm.controller.admin.mat.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 物料主档分页 Request VO")
@Data
public class MatPageReqVO extends PageParam {

    @Schema(description = "物料分类编号", example = "8083")
    private Long catId;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称", example = "李四")
    private String name;

    @Schema(description = "规格型号")
    private String spec;

    @Schema(description = "图号")
    private String drawingNo;

    @Schema(description = "物料类型", example = "1")
    private String matType;

    @Schema(description = "基准计量单位编号", example = "35")
    private Long baseUomId;

    @Schema(description = "是否批次管控")
    private Boolean batchFlag;

    @Schema(description = "是否SN管控")
    private Boolean snFlag;

    @Schema(description = "保质期（天）")
    private Integer shelfLifeDays;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}