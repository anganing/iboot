package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;

/**
 * WMS-仓储策略新增/修改 Request VO。
 *
 * <p>策略配置收结构化的 {@link StrategyConfigVO} 而不是 JSON 字符串：手写 JSON 既没有字段校验，
 * 也没法让人知道这个策略类型能筛哪些字段，配错了要等货真的上错架才发现。
 */
@Schema(description = "管理后台 - WMS-仓储策略新增/修改 Request VO")
@Data
public class StrategySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略编码不能为空")
    private String code;

    @Schema(description = "策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略名称不能为空")
    private String name;

    @Schema(description = "策略类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略类型不能为空")
    @InEnum(WmsStrategyTypeEnum.class)
    private String strategyType;

    /**
     * 适用仓库编号，留空表示全局。
     *
     * <p>不加 {@code @NotNull}：页面上"不选仓库"就是"这条策略对所有仓库生效"，是常用配法。
     * 落库时由 Service 归一成 {@code WmsConstants.GLOBAL_WH_ID}，因为 {@code wh_id} 是 NOT NULL 列。
     */
    @Schema(description = "适用仓库编号，留空表示全局", example = "1")
    private Long whId;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略版本号不能为空")
    private String versionNo;

    @Schema(description = "策略配置：筛选条件、排序链与类型专属选项",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "策略配置不能为空")
    @Valid
    private StrategyConfigVO config;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级，数值越大越优先不能为空")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsStrategyStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    private String remark;

}
