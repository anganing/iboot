package com.iboot.pf.module.wms.dal.dataobject.containermgr;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-容器主档 DO
 *
 * @author iboot
 */
@TableName("wms_container")
@KeySequence("wms_container_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 容器编号或条码
     */
    private String containerNo;
    /**
     * 容器类型编号
     */
    private Long typeId;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 当前仓库编号
     */
    private Long whId;
    /**
     * 当前库位编号，0表示未落位
     */
    private Long locId;
    /**
     * 实际长度，单位毫米
     */
    private BigDecimal length;
    /**
     * 实际宽度，单位毫米
     */
    private BigDecimal width;
    /**
     * 实际高度，单位毫米
     */
    private BigDecimal height;
    /**
     * 实际毛重
     */
    private BigDecimal grossWeight;
    /**
     * 状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED
     */
    private String status;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}