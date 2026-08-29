package com.iboot.pf.module.mdm.controller.admin.defectcode.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 缺陷代码分页 Request VO")
@Data
public class DefectCodePageReqVO extends PageParam {

    @Schema(description = "缺陷编码")
    private String code;

    @Schema(description = "缺陷名称", example = "划伤")
    private String name;

    @Schema(description = "缺陷等级", example = "MAJOR")
    private String level;

    @Schema(description = "适用工序编号")
    private Long opId;

    @Schema(description = "适用物料分类编号")
    private Long matCatId;

    @Schema(description = "缺陷分类树父级编号")
    private Long parentId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
