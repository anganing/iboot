package com.iboot.pf.module.wms.dal.dataobject.batch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/** 批次主档。 */
@TableName("wms_batch")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsBatchDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 物料名称快照。 */
    private String matName;

    /** 批次号；无批次物料使用默认批次。 */
    private String lotNo;

    /** 序列号；SN 物料以数量为 1 的批次承载。 */
    private String snNo;

    /** 供应商编号。 */
    private Long supplierId;

    /** 供应商编码快照。 */
    private String supplierCode;

    /** 供应商批次号。 */
    private String supplierLotNo;

    /** 炉批号。 */
    private String furnaceLotNo;

    /** 生产日期。 */
    private LocalDate produceDate;

    /** 失效日期。 */
    private LocalDate expiryDate;

    /** 状态：0-启用，1-停用。 */
    private Integer status;

    /** 备注。 */
    private String remark;

}
