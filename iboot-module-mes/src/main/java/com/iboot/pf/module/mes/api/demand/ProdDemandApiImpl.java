package com.iboot.pf.module.mes.api.demand;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.demand.dto.CreateProdDemandReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandActionReqDTO;
import com.iboot.pf.module.mes.api.demand.dto.ProdDemandRespDTO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.service.proddemand.MesProdDemandDomainService;
import com.iboot.pf.module.mes.service.proddemand.ProdDemandService;
import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 生产需求 API 实现。
 *
 * <p>门面只做转换与转调：建单转调 codegen {@link ProdDemandService}（它自己按
 * {@code mes_prod_demand.idempotency_key} 的唯一索引判重），改状态的动作转调
 * {@link MesProdDemandDomainService}——需求状态只能由领域动作推进。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法，理由同
 * {@code ProdOrderApiImpl}：门面给别的模块调，调用方的登录用户与需求的数据范围无关。
 */
@Service
@Validated
@DataPermission(enable = false)
public class ProdDemandApiImpl implements ProdDemandApi {

    @Resource
    private ProdDemandService prodDemandService;
    @Resource
    private MesProdDemandDomainService prodDemandDomainService;

    @Override
    public Long createProdDemand(CreateProdDemandReqDTO reqDTO) {
        return prodDemandService.createProdDemand(toSaveReqVO(reqDTO));
    }

    @Override
    public void cancelProdDemand(ProdDemandActionReqDTO reqDTO) {
        MesConfirmDemandBO bo = BeanUtils.toBean(reqDTO, MesConfirmDemandBO.class);
        // DTO 叫 reason、BO 叫 remark：取消原因是事后追查"这批需求为什么没投产"的唯一线索，
        // 靠 BeanUtils 按名字对会静默丢掉，必须显式搬
        bo.setRemark(reqDTO.getReason());
        prodDemandDomainService.cancelDemand(bo);
    }

    @Override
    public ProdDemandRespDTO getProdDemand(Long id) {
        if (id == null) {
            return null;
        }
        ProdDemandDO demand = prodDemandService.getProdDemandList(List.of(id)).stream().findFirst().orElse(null);
        return BeanUtils.toBean(demand, ProdDemandRespDTO.class);
    }

    @Override
    public List<ProdDemandRespDTO> getProdDemandList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return BeanUtils.toBean(prodDemandService.getProdDemandList(ids), ProdDemandRespDTO.class);
    }

    /**
     * 建需求命令转 codegen 的建单 VO。
     *
     * <p>三处显式补值，都是为了满足 codegen VO 上的 {@code @NotNull} / {@code @NotEmpty}：
     * 两个"0 表示无"的项目外键与优先级留空落 0；状态落 {@code DRAFT}——门面 DTO 刻意不带状态
     * （"新建需求固定落 DRAFT，确认与取消都是独立动作"），而 codegen 建单要求状态非空且不覆盖入参值。
     * 这里取枚举名而不是写字面量。
     */
    private ProdDemandSaveReqVO toSaveReqVO(CreateProdDemandReqDTO reqDTO) {
        ProdDemandSaveReqVO saveReqVO = BeanUtils.toBean(reqDTO, ProdDemandSaveReqVO.class);
        saveReqVO.setId(null);
        saveReqVO.setProjectId(noneIfNull(reqDTO.getProjectId()));
        saveReqVO.setProjectTaskId(noneIfNull(reqDTO.getProjectTaskId()));
        saveReqVO.setPriority(reqDTO.getPriority() == null ? 0 : reqDTO.getPriority());
        saveReqVO.setStatus(MesDemandStatusEnum.DRAFT.name());
        return saveReqVO;
    }

    private Long noneIfNull(Long id) {
        return id == null ? MesConstants.NONE_ID : id;
    }

}
