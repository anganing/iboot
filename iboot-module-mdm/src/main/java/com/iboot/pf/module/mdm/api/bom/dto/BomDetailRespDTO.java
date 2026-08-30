package com.iboot.pf.module.mdm.api.bom.dto;

import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 物料清单（BOM）详情 Response DTO
 *
 * @author iboot
 */
@Data
public class BomDetailRespDTO {

    /** BOM 编号 */
    private Long id;

    /** 物料编号 */
    private Long matId;

    /** 版本号 */
    private String version;

    /** 状态：0-启用，1-停用 */
    private Integer status;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expiryDate;

    /** BOM 明细列表 */
    private List<BomLineRespDTO> lines;

    /** 父件物料信息（可选，用于展示） */
    private MaterialRespDTO mat;

}
