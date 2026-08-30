package com.iboot.pf.module.mdm.api.bom.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物料清单（BOM）明细 Response DTO
 *
 * @author iboot
 */
@Data
public class BomLineRespDTO {

    /** 明细编号 */
    private Long id;

    /** BOM 编号 */
    private Long bomId;

    /** 行号 */
    private Integer lineNo;

    /** 子件物料编号 */
    private Long matId;

    /** 子件物料编码快照 */
    private String matCode;

    /** 子件物料名称快照 */
    private String matName;

    /** 数量 */
    private BigDecimal qty;

    /** 损耗率 */
    private BigDecimal scrapRate;

    /** 供料方式：PICK-领料，BACKFLUSH-倒冲，PHANTOM-虚拟件，DIRECT-直送线边 */
    private String issueMethod;

    /** 用料绑定工序顺序号 */
    private Integer routeOpSeq;

    /** 位号/装配位置 */
    private String positionNo;

    /** 行级生效日期 */
    private LocalDate effectiveDate;

    /** 行级失效日期 */
    private LocalDate expiryDate;

    /** 计量单位编号 */
    private Long uomId;

    /** 备注 */
    private String remark;

}
