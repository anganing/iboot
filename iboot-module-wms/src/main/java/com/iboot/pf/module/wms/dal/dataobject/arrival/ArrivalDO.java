package com.iboot.pf.module.wms.dal.dataobject.arrival;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-到货登记 DO
 *
 * @author iboot
 */
@TableName("wms_arrival")
@KeySequence("wms_arrival_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 到货登记单号
     */
    private String arrivalNo;
    /**
     * 来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER
     */
    private String sourceType;
    /**
     * 来源单号
     */
    private String sourceNo;
    /**
     * 供应商编号
     */
    private Long supplierId;
    /**
     * 供应商编码快照
     */
    private String supplierCode;
    /**
     * 供应商名称快照
     */
    private String supplierName;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 目标仓库编号
     */
    private Long whId;
    /**
     * 预计到货日期
     */
    private LocalDate expectDate;
    /**
     * 实际到货时间
     */
    private LocalDateTime arrivalTime;
    /**
     * 状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}