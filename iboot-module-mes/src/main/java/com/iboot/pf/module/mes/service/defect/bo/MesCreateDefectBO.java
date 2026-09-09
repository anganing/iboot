package com.iboot.pf.module.mes.service.defect.bo;

import com.iboot.pf.module.mes.service.defect.MesDefectDomainService;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 缺陷录入参数。
 *
 * <p>缺陷记录是"结论提交的从属数据"：它依附于质检任务产生，没有独立的业务生命周期，
 * 不做命令幂等——缺陷录错了删掉重录是常规操作，幂等拦截反而挡住纠错，追溯靠
 * {@code mes_defect} 的创建人与创建时间。这与检验结果录入（T2-05）的取舍一致。
 *
 * <p>物料上下文不随本 BO 传入：缺陷挂在质检任务上，物料以任务快照的 {@code matId}
 * 为准，调用方传了也不认，避免缺陷的物料归属与任务的物料归属各说各话。
 *
 * @author iboot
 */
@Data
public class MesCreateDefectBO {

    /** 质检任务编号，必填。 */
    private Long taskId;

    /** 关联检验结果编号，0 表示未关联具体检验项，可空。 */
    private Long resultId;

    /** 缺陷代码编号，必填，必须在 {@link MesDefectDomainService#getAvailableDefectCodes} 的可选范围内。 */
    private Long defectCodeId;

    /** 缺陷数量，必填且大于零。 */
    private BigDecimal qty;

    /**
     * 检验工序上下文，可空。
     *
     * <p>缺陷码的"按工序限定"维度用它过滤。工序任务产生的检验（FAI / IPQC / FQC）由调用方传入，
     * 到货与发货检验（IQC / OQC）没有工序上下文则不传——此时限工序的缺陷码不可选。
     */
    private Long opId;

    /** 缺陷描述。 */
    private String description;

    /** 缺陷位置。 */
    private String location;

    /** 备注。 */
    private String remark;

}
