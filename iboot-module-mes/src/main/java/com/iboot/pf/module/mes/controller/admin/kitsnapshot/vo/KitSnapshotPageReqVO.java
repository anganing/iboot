package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-齐套检查快照分页 Request VO")
@Data
public class KitSnapshotPageReqVO extends PageParam {

    @Schema(description = "齐套检查编号")
    private String checkNo;

    @Schema(description = "检查范围：PROD_ORDER、WORK_ORDER、OP_TASK")
    private String checkScope;

    @Schema(description = "生产工单编号", example = "10712")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0表示整单检查", example = "17517")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0表示非工序检查", example = "13473")
    private Long opTaskId;

    @Schema(description = "检查明细行号")
    private Integer lineNo;

    @Schema(description = "需求物料编号", example = "17036")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "赵六")
    private String matName;

    @Schema(description = "需求数量")
    private BigDecimal reqQty;

    @Schema(description = "检查时可用数量")
    private BigDecimal availQty;

    @Schema(description = "检查时本需求已预留数量")
    private BigDecimal reservedQty;

    @Schema(description = "检查时已领用数量")
    private BigDecimal issuedQty;

    @Schema(description = "短缺数量")
    private BigDecimal shortQty;

    @Schema(description = "实际采用替代料编号，0表示未采用", example = "1026")
    private Long altMatId;

    @Schema(description = "检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE")
    private String result;

    @Schema(description = "检查时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] checkTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}