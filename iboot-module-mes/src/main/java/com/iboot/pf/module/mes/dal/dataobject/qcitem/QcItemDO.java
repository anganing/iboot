package com.iboot.pf.module.mes.dal.dataobject.qcitem;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-检验项目 DO
 *
 * @author iboot
 */
@TableName("mes_qc_item")
@KeySequence("mes_qc_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 检验方案编号
     */
    private Long planId;
    /**
     * 检验项编码
     */
    private String itemCode;
    /**
     * 检验项名称
     */
    private String itemName;
    /**
     * 检验项类型：NUMERIC、TEXT、BOOLEAN
     */
    private String itemType;
    /**
     * 检验值单位
     */
    private String unit;
    /**
     * 标准值
     */
    private String stdValue;
    /**
     * 数值上限
     */
    private BigDecimal upperLimit;
    /**
     * 数值下限
     */
    private BigDecimal lowerLimit;
    /**
     * 是否必检
     */
    private Boolean requiredFlag;
    /**
     * 排序号
     */
    private Integer sortNo;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}