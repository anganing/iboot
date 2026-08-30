package com.iboot.pf.module.mdm.api.material.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料主档 Response DTO
 *
 * @author iboot
 */
@Data
public class MaterialRespDTO {

    /** 物料编号 */
    private Long id;

    /** 物料分类编号 */
    private Long catId;

    /** 物料编码 */
    private String code;

    /** 物料名称 */
    private String name;

    /** 规格型号 */
    private String spec;

    /** 图号 */
    private String drawingNo;

    /** 物料类型：RAW-原材料，SEMI-半成品，FIN-成品，PACK-包装物，MRO-辅料耗材 */
    private String matType;

    /** 基准计量单位编号 */
    private Long baseUomId;

    /** 是否批次管控 */
    private Boolean batchFlag;

    /** 是否 SN 管控 */
    private Boolean snFlag;

    /** 保质期天数 */
    private Integer shelfLifeDays;

    /** 净重 */
    private BigDecimal netWeight;

    /** 毛重 */
    private BigDecimal grossWeight;

    /** 重量单位编号 */
    private Long weightUomId;

    /** 长，毫米 */
    private BigDecimal length;

    /** 宽，毫米 */
    private BigDecimal width;

    /** 高，毫米 */
    private BigDecimal height;

    /** 体积，立方毫米 */
    private BigDecimal volume;

    /** 计划策略：MTO-按单，MTS-备库 */
    private String planStrategy;

    /** 生产/采购提前期（天） */
    private Integer leadTimeDays;

    /** 安全库存 */
    private BigDecimal safetyStock;

    /** 最小订货量 */
    private BigDecimal minOrderQty;

    /** 订货倍数 */
    private BigDecimal orderMultiple;

    /** 默认仓库编号 */
    private Long defaultWhId;

    /** 默认库位编号 */
    private Long defaultLocId;

    /** 标准包装量 */
    private BigDecimal packQty;

    /** ABC 分类：A/B/C */
    private String abcClass;

    /** 换型分组 */
    private String changeoverGroup;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
