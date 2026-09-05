package com.iboot.pf.module.mdm.api.matalt.dto;

import lombok.Data;

/**
 * 替代料 Response DTO
 *
 * <p>MES 齐套检查在主料不足时按 {@code priority} 升序逐个试替代料。
 *
 * @author iboot
 */
@Data
public class MatAltRespDTO {

    /** 替代料关系编号 */
    private Long id;

    /** 原物料编号 */
    private Long matId;

    /** 替代物料编号 */
    private Long altMatId;

    /** 优先级，数值越小越优先 */
    private Integer priority;

    /** 备注 */
    private String remark;

}
