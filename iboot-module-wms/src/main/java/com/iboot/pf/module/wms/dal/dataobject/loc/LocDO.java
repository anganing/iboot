package com.iboot.pf.module.wms.dal.dataobject.loc;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-库位 DO
 *
 * @author iboot
 */
@TableName("wms_loc")
@KeySequence("wms_loc_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 仓库编号
     */
    private Long whId;
    /**
     * 库区编号
     */
    private Long zoneId;
    /**
     * 库位编码
     */
    private String code;
    /**
     * 库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX
     */
    private String locType;
    /**
     * 巷道号
     */
    private String aisleNo;
    /**
     * 排号
     */
    private String rowNo;
    /**
     * 列号
     */
    private String colNo;
    /**
     * 层号
     */
    private String levelNo;
    /**
     * 纵深号
     */
    private Integer depthNo;
    /**
     * 容器最大长度，单位毫米
     */
    private BigDecimal maxLength;
    /**
     * 容器最大宽度，单位毫米
     */
    private BigDecimal maxWidth;
    /**
     * 容器最大高度，单位毫米
     */
    private BigDecimal maxHeight;
    /**
     * 最大承重
     */
    private BigDecimal maxWeight;
    /**
     * 最大容积
     */
    private BigDecimal maxVolume;
    /**
     * 最大容器数量
     */
    private Integer maxContainerQty;
    /**
     * 尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM
     */
    private String dimensionCheckMode;
    /**
     * 状态：EMPTY、OCCUPIED、LOCKED、DISABLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}