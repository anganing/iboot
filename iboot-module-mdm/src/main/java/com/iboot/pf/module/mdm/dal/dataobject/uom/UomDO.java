package com.iboot.pf.module.mdm.dal.dataobject.uom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 计量单位。 */
@TableName("mdm_uom")
@Data
@EqualsAndHashCode(callSuper = true)
public class UomDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 计量单位编码。 */
    private String code;

    /** 计量单位名称。 */
    private String name;

    /** 状态：0-启用，1-停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

}
