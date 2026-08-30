package com.iboot.pf.module.mdm.api.bom.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 物料清单（BOM）Response DTO
 *
 * @author iboot
 */
@Data
public class BomRespDTO {

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

}
