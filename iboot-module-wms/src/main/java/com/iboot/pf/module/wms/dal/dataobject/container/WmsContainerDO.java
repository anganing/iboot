package com.iboot.pf.module.wms.dal.dataobject.container;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 容器主档。 */
@TableName("wms_container")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsContainerDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 容器编号或条码。 */
    private String containerNo;

    /** 容器类型编号。 */
    private Long typeId;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 当前仓库编号。 */
    private Long whId;

    /** 当前库位编号，0 表示未落位。 */
    private Long locId;

    /** 实际长度，单位毫米。 */
    private BigDecimal length;

    /** 实际宽度，单位毫米。 */
    private BigDecimal width;

    /** 实际高度，单位毫米。 */
    private BigDecimal height;

    /** 实际毛重。 */
    private BigDecimal grossWeight;

    /** 状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED。 */
    private String status;

    /** 备注。 */
    private String remark;

    /** 乐观锁版本，更新余额时按版本号比对，冲突即失败。 */
    private Integer version;

}
