package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-入库单分页 Request VO")
@Data
public class InOrderPageReqVO extends PageParam {

    @Schema(description = "入库单号")
    private String inNo;

    @Schema(description = "入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN", example = "1")
    private String bizType;

    @Schema(description = "直接来源类型", example = "2")
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "17337")
    private Long sourceLineId;

    @Schema(description = "原始来源类型", example = "2")
    private String originType;

    @Schema(description = "原始来源单号")
    private String originNo;

    @Schema(description = "到货登记编号", example = "16148")
    private Long arrivalId;

    @Schema(description = "供应商编号", example = "21246")
    private Long supplierId;

    @Schema(description = "客户编号", example = "6109")
    private Long customerId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    private String countOrderNo;

    @Schema(description = "目标库存组织编号", example = "22369")
    private Long invOrgId;

    @Schema(description = "目标工厂编号", example = "20131")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", example = "1028")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "26608")
    private Long projectTaskId;

    @Schema(description = "目标仓库编号", example = "10487")
    private Long whId;

    @Schema(description = "目标仓库编码快照")
    private String whCode;

    @Schema(description = "来源库存组织编号，0表示非调拨", example = "24871")
    private Long fromInvOrgId;

    @Schema(description = "来源仓库编号，0表示非调拨", example = "31447")
    private Long fromWhId;

    @Schema(description = "是否需要质检")
    private Boolean needQc;

    @Schema(description = "来料检验模式")
    private String iqcMode;

    @Schema(description = "入库质量状态", example = "2")
    private String qualityStatus;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", example = "1")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}