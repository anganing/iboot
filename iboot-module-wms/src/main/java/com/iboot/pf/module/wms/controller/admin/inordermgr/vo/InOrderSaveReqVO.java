package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOriginTypeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;

@Schema(description = "管理后台 - WMS-入库单新增/修改 Request VO")
@Data
public class InOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "入库单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库单号不能为空")
    private String inNo;

    @Schema(description = "入库业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库业务类型不能为空")
    @InEnum(WmsInBizTypeEnum.class)
    private String bizType;

    @Schema(description = "直接来源类型")
    @InEnum(WmsSourceModuleEnum.class)
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "1")
    private Long sourceLineId;

    @Schema(description = "原始来源类型")
    @InEnum(WmsOriginTypeEnum.class)
    private String originType;

    @Schema(description = "原始来源单号")
    private String originNo;

    @Schema(description = "到货登记编号", example = "1")
    private Long arrivalId;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    private String countOrderNo;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "目标工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    @Schema(description = "目标仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "目标仓库编码快照不能为空")
    private String whCode;

    @Schema(description = "来源库存组织编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库存组织编号，0表示非调拨不能为空")
    private Long fromInvOrgId;

    @Schema(description = "来源仓库编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源仓库编号，0表示非调拨不能为空")
    private Long fromWhId;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否需要质检不能为空")
    private Boolean needQc;

    @Schema(description = "来料检验模式")
    @InEnum(WmsIqcModeEnum.class)
    private String iqcMode;

    @Schema(description = "入库质量状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatus;

    @Schema(description = "外部设备触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备触发方式不能为空")
    @InEnum(WmsHookModeEnum.class)
    private String hookMode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsOrderStatusEnum.class)
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因")
    private String remark;

}
