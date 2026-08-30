package com.iboot.pf.module.wms.dal.dataobject.warehouse;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 库区。 */
@TableName("wms_zone")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsZoneDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 仓库编号。 */
    private Long whId;

    /** 库区编码。 */
    private String code;

    /** 库区名称。 */
    private String name;

    /** 库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING。 */
    private String zoneType;

    /** 是否允许混放物料。 */
    private Boolean allowMixedMat;

    /** 是否允许混放批次。 */
    private Boolean allowMixedLot;

    /** 温区要求。 */
    private String tempRange;

    /** 状态：0-启用，1-停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

}
