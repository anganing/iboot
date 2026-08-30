package com.iboot.pf.module.wms.dal.dataobject.container;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 容器类型。 */
@TableName("wms_container_type")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsContainerTypeDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 容器类型编码。 */
    private String code;

    /** 容器类型名称。 */
    private String name;

    /** 标准长度，单位毫米。 */
    private BigDecimal stdLength;

    /** 标准宽度，单位毫米。 */
    private BigDecimal stdWidth;

    /** 标准高度，单位毫米。 */
    private BigDecimal stdHeight;

    /** 最大承重。 */
    private BigDecimal maxWeight;

    /** 最大容积。 */
    private BigDecimal maxVolume;

    /** 状态：0-启用，1-停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

}
