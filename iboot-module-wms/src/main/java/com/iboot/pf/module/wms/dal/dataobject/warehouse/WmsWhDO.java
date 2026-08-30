package com.iboot.pf.module.wms.dal.dataobject.warehouse;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 仓库。 */
@TableName("wms_wh")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsWhDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 工厂编号。 */
    private Long plantId;

    /** 仓库编码。 */
    private String code;

    /** 仓库名称。 */
    private String name;

    /** 仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓。 */
    private String whType;

    /** 管理方式：MANUAL、WCS、AGV、MIXED。 */
    private String managedBy;

    /** 外部设备系统编码。 */
    private String externalSystemCode;

    /** 状态：0-启用，1-停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

}
