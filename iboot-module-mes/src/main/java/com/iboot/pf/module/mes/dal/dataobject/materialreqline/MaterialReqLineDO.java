package com.iboot.pf.module.mes.dal.dataobject.materialreqline;

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
 * MES-领退补料申请明细 DO
 *
 * @author iboot
 */
@TableName("mes_material_req_line")
@KeySequence("mes_material_req_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialReqLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 领退补料申请编号
     */
    private Long reqId;
    /**
     * 行号，同一申请下唯一
     */
    private Integer lineNo;
    /**
     * 路线工序用料编号，0 表示非 BOM 用料，来自 mdm_route_op_mat
     */
    private Long routeOpMatId;
    /**
     * 物料编号，来自 mdm_mat
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
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 申请数量
     */
    private BigDecimal qty;
    /**
     * WMS 已处理数量，由 WMS 回写
     */
    private BigDecimal processedQty;
    /**
     * 计量单位编号，来自 mdm_uom
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 指定退料批次编号，0 表示不指定批次，来自 wms_batch
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 是否必须在开工前发料
     */
    private Boolean mustIssueBeforeStart;
    /**
     * 明细状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
