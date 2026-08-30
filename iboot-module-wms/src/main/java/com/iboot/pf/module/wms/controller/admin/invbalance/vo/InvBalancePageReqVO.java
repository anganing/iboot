package com.iboot.pf.module.wms.controller.admin.invbalance.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-多维库存余额分页 Request VO")
@Data
public class InvBalancePageReqVO extends PageParam {

    @Schema(description = "库存组织编号", example = "29559")
    private Long invOrgId;

    @Schema(description = "工厂编号", example = "1418")
    private Long plantId;

    @Schema(description = "仓库编号", example = "7799")
    private Long whId;

    @Schema(description = "库位编号", example = "27100")
    private Long locId;

    @Schema(description = "物料编号", example = "16866")
    private Long matId;

    @Schema(description = "批次编号；无批次物料挂默认批次", example = "12889")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", example = "18354")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目归属", example = "18162")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务归属", example = "8693")
    private Long projectTaskId;

    @Schema(description = "库存范围：COMMON、PROJECT_DEDICATED")
    private String stockScope;

    @Schema(description = "质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN", example = "1")
    private String qualityStatus;

    @Schema(description = "现存数量，基准单位")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量，基准单位")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量，基准单位")
    private BigDecimal qtyFrozen;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "赵六")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "基准单位编码快照")
    private String uomCode;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "仓库编码快照")
    private String whCode;

    @Schema(description = "库位编码快照")
    private String locCode;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}