package com.iboot.pf.module.mdm.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 工艺路线复制新版本 Request VO")
@Data
public class RouteCopyReqVO {

    @Schema(description = "原工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "原工艺路线不能为空")
    private Long id;

    @Schema(description = "新版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V2")
    @NotEmpty(message = "新版本号不能为空")
    private String version;

    @Schema(description = "生效日期，为空则取当天")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveDate;

}
