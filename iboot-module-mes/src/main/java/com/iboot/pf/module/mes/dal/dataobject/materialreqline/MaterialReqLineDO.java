package com.iboot.pf.module.mes.dal.dataobject.materialreqline;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-领退补料申请明细 DO
 *
 * @author iboot
 */
@TableName("mes_material_req_line")
@KeySequence("mes_material_req_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReqLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 领退补料申请编号
     */
    private Long reqId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 路线工序用料编号
     */
    private Long routeOpMatId;
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
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 申请数量
     */
    private BigDecimal qty;
    /**
     * WMS已处理数量
     */
    private BigDecimal processedQty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 指定退料批次编号
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 是否必须在开工前发料
     */
    private Boolean mustIssueBeforeStart;
    /**
     * 明细状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}