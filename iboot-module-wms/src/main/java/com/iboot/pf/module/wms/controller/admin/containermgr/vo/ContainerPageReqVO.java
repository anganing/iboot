package com.iboot.pf.module.wms.controller.admin.containermgr.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-容器主档分页 Request VO")
@Data
public class ContainerPageReqVO extends PageParam {

    @Schema(description = "容器编号或条码")
    private String containerNo;

    @Schema(description = "容器类型编号", example = "1")
    private Long typeId;

    @Schema(description = "当前仓库编号", example = "1")
    private Long whId;

    @Schema(description = "当前库位编号，0表示未落位", example = "1")
    private Long locId;

    @Schema(description = "状态", example = "EMPTY")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
