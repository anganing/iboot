package com.iboot.pf.module.wms.dal.dataobject.warehouse;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 库位。 */
@TableName("wms_loc")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsLocDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 仓库编号。 */
    private Long whId;

    /** 库区编号。 */
    private Long zoneId;

    /** 库位编码。 */
    private String code;

    /** 库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX。 */
    private String locType;

    /** 巷道号。 */
    private String aisleNo;

    /** 排号。 */
    private String rowNo;

    /** 列号。 */
    private String colNo;

    /** 层号。 */
    private String levelNo;

    /** 纵深号。 */
    private Integer depthNo;

    /** 容器最大长度，单位毫米。 */
    private BigDecimal maxLength;

    /** 容器最大宽度，单位毫米。 */
    private BigDecimal maxWidth;

    /** 容器最大高度，单位毫米。 */
    private BigDecimal maxHeight;

    /** 最大承重。 */
    private BigDecimal maxWeight;

    /** 最大容积。 */
    private BigDecimal maxVolume;

    /** 最大容器数量。 */
    private Integer maxContainerQty;

    /** 尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM。 */
    private String dimensionCheckMode;

    /** 状态：EMPTY、OCCUPIED、LOCKED、DISABLED。 */
    private String status;

    /** 备注。 */
    private String remark;

}
