package com.iboot.pf.module.aps.dal.dataobject.projectionmat;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 物料投影 DO
 *
 * @author iboot
 */
@TableName("aps_projection_mat")
@KeySequence("aps_projection_mat_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionMatDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程场景编号
     */
    private Long scenarioId;
    /**
     * MDM物料编号
     */
    private Long sourceMatId;
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
     * 物料类型快照
     */
    private String matType;
    /**
     * 基准计量单位编号
     */
    private Long baseUomId;
    /**
     * 基准计量单位编码快照
     */
    private String baseUomCode;
    /**
     * 是否批次管控
     */
    private Boolean batchFlag;
    /**
     * 当前有效BOM编号，0表示无
     */
    private Long bomId;
    /**
     * 当前有效BOM版本
     */
    private String bomVersion;
    /**
     * 当前有效工艺路线编号，0表示无
     */
    private Long routeId;
    /**
     * 当前有效工艺路线版本
     */
    private String routeVersion;
    /**
     * 来源数据更新时间
     */
    private LocalDateTime sourceUpdateTime;
    /**
     * 投影快照时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 备注
     */
    private String remark;


}