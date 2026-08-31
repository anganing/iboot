package com.iboot.pf.module.mes.dal.dataobject.qcitem;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * MES-检验项目 DO
 *
 * @author iboot
 */
@TableName("mes_qc_item")
@KeySequence("mes_qc_item_seq")
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
     * 检验项编码，同一方案下唯一
     */
    private String itemCode;
    /**
     * 检验项名称
     */
    private String itemName;
    /**
     * 检验项类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcItemTypeEnum}
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
     * 数值上限，仅数值型检验项取值
     */
    private BigDecimal upperLimit;
    /**
     * 数值下限，仅数值型检验项取值
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
     * 状态
     *
     * 枚举 {@link com.iboot.pf.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
