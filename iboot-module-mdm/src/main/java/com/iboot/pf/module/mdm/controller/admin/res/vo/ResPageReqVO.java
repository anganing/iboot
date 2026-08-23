package com.iboot.pf.module.mdm.controller.admin.res.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 生产资源分页 Request VO")
@Data
public class ResPageReqVO extends PageParam {

    @Schema(description = "工作中心编号", example = "20144")
    private Long wcId;

    @Schema(description = "资源类型", example = "1")
    private String resType;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称", example = "王五")
    private String name;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}