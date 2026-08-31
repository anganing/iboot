package com.iboot.pf.module.mes.dal.dataobject.kitsnapshot;

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
import java.time.LocalDateTime;

/**
 * MES-齐套检查快照 DO
 *
 * <p>账表：由齐套检查动作写入，记录检查那一刻的库存口径，不接受人工新增修改删除。
 *
 * @author iboot
 */
@TableName("mes_kit_snapshot")
@KeySequence("mes_kit_snapshot_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitSnapshotDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 齐套检查编号，由 infra 编码规则 MES_KIT_CHECK_NO 分配
     */
    private String checkNo;
    /**
     * 检查范围
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum}
     */
    private String checkScope;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 作业工单编号，0 表示整单检查
     */
    private Long workOrderId;
    /**
     * 工序任务编号，0 表示非工序检查
     */
    private Long opTaskId;
    /**
     * 检查明细行号，与检查编号组成唯一索引
     */
    private Integer lineNo;
    /**
     * 需求物料编号，来自 mdm_mat
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 需求数量
     */
    private BigDecimal reqQty;
    /**
     * 检查时可用数量
     */
    private BigDecimal availQty;
    /**
     * 检查时本需求已预留数量
     */
    private BigDecimal reservedQty;
    /**
     * 检查时已领用数量
     */
    private BigDecimal issuedQty;
    /**
     * 短缺数量
     */
    private BigDecimal shortQty;
    /**
     * 实际采用替代料编号，0 表示未采用
     */
    private Long altMatId;
    /**
     * 检查结果
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesKitStatusEnum}
     */
    private String result;
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    /**
     * 备注
     */
    private String remark;

}
