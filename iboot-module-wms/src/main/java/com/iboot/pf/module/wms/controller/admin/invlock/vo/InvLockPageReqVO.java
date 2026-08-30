package com.iboot.pf.module.wms.controller.admin.invlock.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存冻结记录分页 Request VO")
@Data
public class InvLockPageReqVO extends PageParam {

    @Schema(description = "冻结编号")
    private String lockNo;

    @Schema(description = "来源业务类型", example = "2")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "库存组织编号", example = "22240")
    private Long invOrgId;

    @Schema(description = "仓库编号", example = "10149")
    private Long whId;

    @Schema(description = "库位编号", example = "4201")
    private Long locId;

    @Schema(description = "物料编号", example = "8353")
    private Long matId;

    @Schema(description = "批次编号", example = "15271")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", example = "13301")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目", example = "19623")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "25767")
    private Long projectTaskId;

    @Schema(description = "质量状态", example = "1")
    private String qualityStatus;

    @Schema(description = "冻结数量，基准单位")
    private BigDecimal qty;

    @Schema(description = "已解冻数量")
    private BigDecimal releasedQty;

    @Schema(description = "冻结原因", example = "不香")
    private String reason;

    @Schema(description = "状态：ACTIVE、PARTIAL、RELEASED、CANCELLED", example = "1")
    private String status;

    @Schema(description = "冻结幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}