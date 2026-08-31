package com.iboot.pf.module.mes.dal.dataobject.defect;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * MES-缺陷记录 DO
 *
 * <p>附件放在独立子表 {@code mes_defect_file}，主表不留 file_* 列。
 *
 * @author iboot
 */
@TableName("mes_defect")
@KeySequence("mes_defect_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 质检任务编号
     */
    private Long taskId;
    /**
     * 关联检验结果编号，0 表示未关联具体检验项
     */
    private Long resultId;
    /**
     * 缺陷代码编号，来自 mdm_defect_code
     */
    private Long defectCodeId;
    /**
     * 缺陷编码快照
     */
    private String defectCode;
    /**
     * 缺陷名称快照
     */
    private String defectName;
    /**
     * 缺陷等级快照
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDefectLevelEnum}
     */
    private String defectLevel;
    /**
     * 缺陷数量
     */
    private BigDecimal qty;
    /**
     * 缺陷描述
     */
    private String description;
    /**
     * 缺陷位置
     */
    private String location;
    /**
     * 备注
     */
    private String remark;

}
