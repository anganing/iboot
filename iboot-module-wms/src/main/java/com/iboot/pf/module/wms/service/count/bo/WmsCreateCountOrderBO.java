package com.iboot.pf.module.wms.service.count.bo;

import lombok.Data;

import java.util.Collection;

/** 创建盘点单的内部参数。 */
@Data
public class WmsCreateCountOrderBO {

    /** 创建幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 盘点单号，为空由编码规则生成。 */
    private String countNo;

    /** 盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC。 */
    private String countType;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 库区编号，0 或空表示全部库区。 */
    private Long zoneId;

    /** 项目编号，0 或空表示不限项目。 */
    private Long projectId;

    /** 限定物料编号集合，为空表示不限物料。 */
    private Collection<Long> matIds;

    /** 是否盲盘。 */
    private Boolean blindCountFlag;

    /** 备注。 */
    private String remark;

}
