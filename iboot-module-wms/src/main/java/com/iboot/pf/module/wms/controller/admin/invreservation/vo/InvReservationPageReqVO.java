package com.iboot.pf.module.wms.controller.admin.invreservation.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存预留分页 Request VO")
@Data
public class InvReservationPageReqVO extends PageParam {

    @Schema(description = "预留编号")
    private String reservationNo;

    @Schema(description = "来源业务类型", example = "1")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务行号")
    private String bizLineNo;

    @Schema(description = "库存组织编号", example = "18730")
    private Long invOrgId;

    @Schema(description = "仓库编号", example = "1837")
    private Long whId;

    @Schema(description = "库位编号", example = "19891")
    private Long locId;

    @Schema(description = "物料编号", example = "11298")
    private Long matId;

    @Schema(description = "批次编号", example = "6680")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", example = "17048")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目", example = "15707")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "19010")
    private Long projectTaskId;

    @Schema(description = "质量状态", example = "1")
    private String qualityStatus;

    @Schema(description = "预留数量，基准单位")
    private BigDecimal qty;

    @Schema(description = "已释放或已核销数量")
    private BigDecimal releasedQty;

    @Schema(description = "状态：ACTIVE、PARTIAL、RELEASED、CONSUMED、CANCELLED", example = "2")
    private String status;

    @Schema(description = "预留失效时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireTime;

    @Schema(description = "预留幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}