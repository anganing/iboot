package com.iboot.pf.module.wms.dal.dataobject.containertype;

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
 * WMS-容器类型 DO
 *
 * @author iboot
 */
@TableName("wms_container_type")
@KeySequence("wms_container_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerTypeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 容器类型编码
     */
    private String code;
    /**
     * 容器类型名称
     */
    private String name;
    /**
     * 标准长度，单位毫米
     */
    private BigDecimal stdLength;
    /**
     * 标准宽度，单位毫米
     */
    private BigDecimal stdWidth;
    /**
     * 标准高度，单位毫米
     */
    private BigDecimal stdHeight;
    /**
     * 最大承重
     */
    private BigDecimal maxWeight;
    /**
     * 最大容积
     */
    private BigDecimal maxVolume;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}