package com.iboot.pf.module.wms.dal.dataobject.wh;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-仓库 DO
 *
 * @author iboot
 */
@TableName("wms_wh")
@KeySequence("wms_wh_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 仓库编码
     */
    private String code;
    /**
     * 仓库名称
     */
    private String name;
    /**
     * 仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓
     */
    private String whType;
    /**
     * 管理方式：MANUAL、WCS、AGV、MIXED
     */
    private String managedBy;
    /**
     * 外部设备系统编码
     */
    private String externalSystemCode;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}