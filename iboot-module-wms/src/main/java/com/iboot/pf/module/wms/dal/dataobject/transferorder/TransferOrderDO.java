package com.iboot.pf.module.wms.dal.dataobject.transferorder;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-库存转移调拨单 DO
 *
 * @author iboot
 */
@TableName("wms_transfer_order")
@KeySequence("wms_transfer_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 转移或调拨单号
     */
    private String transferNo;
    /**
     * 转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER
     */
    private String transferType;
    /**
     * 来源业务类型
     */
    private String sourceType;
    /**
     * 来源业务单号
     */
    private String sourceBizNo;
    /**
     * 来源库存组织编号
     */
    private Long fromInvOrgId;
    /**
     * 目标库存组织编号
     */
    private Long toInvOrgId;
    /**
     * 来源仓库编号
     */
    private Long fromWhId;
    /**
     * 目标仓库编号
     */
    private Long toWhId;
    /**
     * 来源项目编号，0表示公共库存
     */
    private Long fromProjectId;
    /**
     * 目标项目编号，0表示公共库存
     */
    private Long toProjectId;
    /**
     * 外部设备触发方式：NONE、WCS、AGV
     */
    private String hookMode;
    /**
     * 状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION
     */
    private String status;
    /**
     * 创建幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}