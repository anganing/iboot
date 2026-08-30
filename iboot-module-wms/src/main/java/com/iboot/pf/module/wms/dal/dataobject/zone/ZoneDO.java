package com.iboot.pf.module.wms.dal.dataobject.zone;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-库区 DO
 *
 * @author iboot
 */
@TableName("wms_zone")
@KeySequence("wms_zone_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDO extends BaseDO {

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
     * 库区编码
     */
    private String code;
    /**
     * 库区名称
     */
    private String name;
    /**
     * 库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING
     */
    private String zoneType;
    /**
     * 是否允许混放物料
     */
    private Boolean allowMixedMat;
    /**
     * 是否允许混放批次
     */
    private Boolean allowMixedLot;
    /**
     * 温区要求
     */
    private String tempRange;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}