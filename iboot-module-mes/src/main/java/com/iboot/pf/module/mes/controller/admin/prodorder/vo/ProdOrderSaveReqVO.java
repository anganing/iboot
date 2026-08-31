package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工单新增/修改 Request VO。
 *
 * <p>不出现在表单里的字段：物料与单位快照、BOM 与工艺路线版本快照由后端从 mdm 门面取值补全；
 * 已完成数量、累计报废数量由报工推进，创建时归零；下达、实际开工、实际完工时间由业务动作写入；
 * 齐套状态由齐套检查写入；乐观锁版本由框架维护。
 */
@Schema(description = "管理后台 - MES-生产工单新增/修改 Request VO")
@Data
public class ProdOrderSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "生产工单号，留空由编码规则分配")
    @Size(max = 64, message = "生产工单号长度不能超过 64")
    private String orderNo;

    @Schema(description = "生产需求编号，0 表示手工建单", example = "1")
    private Long demandId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目任务编号不能为空")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产物料编号不能为空")
    private Long matId;

    @Schema(description = "计划生产数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划生产数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划生产数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "锁定的 BOM 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "BOM 编号不能为空")
    private Long bomId;

    @Schema(description = "锁定的工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工艺路线编号不能为空")
    private Long routeId;

    @Schema(description = "工单交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工单交期不能为空")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级不能小于零")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesProdOrderStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
