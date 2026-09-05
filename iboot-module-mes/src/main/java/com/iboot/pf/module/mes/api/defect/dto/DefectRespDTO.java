package com.iboot.pf.module.mes.api.defect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 缺陷记录跨模块只读事实。
 *
 * <p>附件在独立子表 {@code mes_defect_file}，需要附件另走附件接口取，不塞进本 DTO。
 */
@Schema(description = "RPC 服务 - 缺陷记录 Response DTO")
@Data
public class DefectRespDTO {

    @Schema(description = "缺陷记录编号", example = "1")
    private Long id;

    @Schema(description = "质检任务编号", example = "1")
    private Long taskId;

    @Schema(description = "关联检验结果编号，0 表示未关联具体检验项", example = "1")
    private Long resultId;

    @Schema(description = "缺陷代码编号", example = "1")
    private Long defectCodeId;

    @Schema(description = "缺陷编码快照", example = "D-0001")
    private String defectCode;

    @Schema(description = "缺陷名称快照")
    private String defectName;

    /** 缺陷等级，取值为 MesDefectLevelEnum 的枚举名。 */
    @Schema(description = "缺陷等级", example = "CRITICAL")
    private String defectLevel;

    @Schema(description = "缺陷数量", example = "2")
    private BigDecimal qty;

    @Schema(description = "缺陷描述")
    private String description;

    @Schema(description = "缺陷位置")
    private String location;

}
