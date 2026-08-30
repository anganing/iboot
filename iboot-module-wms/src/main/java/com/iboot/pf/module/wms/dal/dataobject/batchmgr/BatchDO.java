package com.iboot.pf.module.wms.dal.dataobject.batchmgr;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-批次主档 DO
 *
 * @author iboot
 */
@TableName("wms_batch")
@KeySequence("wms_batch_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 批次号；无批次物料使用默认批次
     */
    private String lotNo;
    /**
     * 序列号；SN物料以数量为1的批次承载
     */
    private String snNo;
    /**
     * 供应商编号
     */
    private Long supplierId;
    /**
     * 供应商编码快照
     */
    private String supplierCode;
    /**
     * 供应商批次号
     */
    private String supplierLotNo;
    /**
     * 炉批号
     */
    private String furnaceLotNo;
    /**
     * 生产日期
     */
    private LocalDate produceDate;
    /**
     * 失效日期
     */
    private LocalDate expiryDate;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}