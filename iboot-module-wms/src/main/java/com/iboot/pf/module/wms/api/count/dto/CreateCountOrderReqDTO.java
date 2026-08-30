package com.iboot.pf.module.wms.api.count.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/** 创建盘点单命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateCountOrderReqDTO extends WmsCommandReqDTO {

    /** 盘点单号，为空由编码规则生成。 */
    @Schema(description = "盘点单号")
    private String countNo;

    /** 盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC。 */
    @Schema(description = "盘点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "FULL")
    @NotNull(message = "盘点类型不能为空")
    private String countType;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    /** 库区编号，0 或空表示全部库区。 */
    @Schema(description = "库区编号", example = "0")
    private Long zoneId;

    /** 项目编号，0 或空表示不限项目。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 限定物料编号集合，为空表示不限物料。 */
    @Schema(description = "限定物料编号集合")
    @NotEmpty(message = "限定物料编号集合不能为空")
    private Collection<Long> matIds;

    /** 是否盲盘。 */
    @Schema(description = "是否盲盘", example = "false")
    private Boolean blindCountFlag;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
