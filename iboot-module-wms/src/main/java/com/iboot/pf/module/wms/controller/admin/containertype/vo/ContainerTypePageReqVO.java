package com.iboot.pf.module.wms.controller.admin.containertype.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-容器类型分页 Request VO")
@Data
public class ContainerTypePageReqVO extends PageParam {

    @Schema(description = "容器类型编码")
    private String code;

    @Schema(description = "容器类型名称", example = "赵六")
    private String name;

    @Schema(description = "标准长度，单位毫米")
    private BigDecimal stdLength;

    @Schema(description = "标准宽度，单位毫米")
    private BigDecimal stdWidth;

    @Schema(description = "标准高度，单位毫米")
    private BigDecimal stdHeight;

    @Schema(description = "最大承重")
    private BigDecimal maxWeight;

    @Schema(description = "最大容积")
    private BigDecimal maxVolume;

    @Schema(description = "状态：0-启用，1-停用", example = "2")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}