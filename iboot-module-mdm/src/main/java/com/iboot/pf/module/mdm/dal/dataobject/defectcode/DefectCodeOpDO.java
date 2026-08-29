package com.iboot.pf.module.mdm.dal.dataobject.defectcode;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 缺陷代码适用工序 DO：无关联行表示该缺陷不限工序。
 *
 * @author iboot
 */
@TableName("mdm_defect_code_op")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectCodeOpDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 缺陷代码编号
     */
    private Long defectCodeId;
    /**
     * 适用工序编号
     */
    private Long opId;

}
