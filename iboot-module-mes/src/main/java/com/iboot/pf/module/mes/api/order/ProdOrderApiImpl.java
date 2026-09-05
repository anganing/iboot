package com.iboot.pf.module.mes.api.order;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderRespDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 生产工单 API 实现。
 *
 * <p>门面只做三件事：把 ReqDTO 转成被调方的入参、调对应的 Service、把结果转成 RespDTO。
 * 分工是硬的：建单转调 codegen {@link ProdOrderService}，改状态的动作一律转调
 * {@link MesProdOrderDomainService}——状态只能由领域动作推进，门面不许自己写状态字段。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法。门面是给别的模块调的，
 * 调用方的登录用户与生产工单的数据范围毫无关系：APS 发布排程要读全厂的工单，不是"当前登录人所在部门的工单"，
 * 跨模块调用天生是系统身份而不是用户身份。打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 */
@Service
@Validated
@DataPermission(enable = false)
public class ProdOrderApiImpl implements ProdOrderApi {

    @Resource
    private ProdOrderService prodOrderService;
    @Resource
    private MesProdOrderDomainService prodOrderDomainService;
    @Resource
    private MesSnapshotService snapshotService;

    @Override
    public Long createProdOrder(CreateProdOrderReqDTO reqDTO) {
        return prodOrderService.createProdOrder(toSaveReqVO(reqDTO));
    }

    @Override
    public void releaseProdOrder(ProdOrderActionReqDTO reqDTO) {
        MesReleaseOrderBO bo = BeanUtils.toBean(reqDTO, MesReleaseOrderBO.class);
        // DTO 叫 reason、BO 叫 remark，同一句话两个字段名，BeanUtils 对不上会静默留 null，
        // 下达流水的备注就永远是空的，只能显式搬一次
        bo.setRemark(reqDTO.getReason());
        prodOrderDomainService.releaseProdOrder(bo);
    }

    @Override
    public void closeProdOrder(ProdOrderActionReqDTO reqDTO) {
        // 关闭返回的多余领料清单在门面这一层无处安放：ProdOrderApi.closeProdOrder 是 void。
        // 见类注释外的偏差说明，跨模块调用方要拿清单需给门面加一个带返回值的重载。
        prodOrderDomainService.closeProdOrder(BeanUtils.toBean(reqDTO, MesCloseOrderBO.class));
    }

    @Override
    public ProdOrderRespDTO getProdOrder(Long id) {
        if (id == null) {
            return null;
        }
        ProdOrderDO prodOrder = prodOrderService.getProdOrderList(List.of(id)).stream().findFirst().orElse(null);
        return BeanUtils.toBean(prodOrder, ProdOrderRespDTO.class);
    }

    @Override
    public List<ProdOrderRespDTO> getProdOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return BeanUtils.toBean(prodOrderService.getProdOrderList(ids), ProdOrderRespDTO.class);
    }

    @Override
    public List<ProdOrderRespDTO> getProdOrderListByPlantId(Long plantId) {
        return BeanUtils.toBean(prodOrderService.getProdOrderListByPlantId(plantId), ProdOrderRespDTO.class);
    }

    /**
     * 建单命令转 codegen 的建单 VO。
     *
     * <p>两处必须显式补值，否则 codegen 侧的 {@code @NotNull} 直接把调用拦下：
     *
     * <p>① 三个"0 表示无"的外键与优先级：门面 DTO 允许留空（注释写的就是"0 表示无项目"），
     * codegen VO 是 {@code @NotNull}，留空落 0 与需求转单那条路径一致（0 号车间表示待指派）。
     *
     * <p>② {@code bomId} / {@code routeId}：门面 DTO 刻意不带这两个字段（版本在下达那一刻才锁），
     * 而 codegen 建单要求两者非空并回读版本号。这里按物料当天的生效版本取，与 codegen 自己的
     * 导入建单（{@code ProdOrderServiceImpl.convertImportItem}）同一口径。这不会绕过版本锁：
     * 下达动作会用下达那一刻的生效版本把四个字段整体覆盖一遍。
     */
    private ProdOrderSaveReqVO toSaveReqVO(CreateProdOrderReqDTO reqDTO) {
        ProdOrderSaveReqVO saveReqVO = BeanUtils.toBean(reqDTO, ProdOrderSaveReqVO.class);
        saveReqVO.setId(null);
        saveReqVO.setDemandId(noneIfNull(reqDTO.getDemandId()));
        saveReqVO.setWorkshopId(noneIfNull(reqDTO.getWorkshopId()));
        saveReqVO.setProjectId(noneIfNull(reqDTO.getProjectId()));
        saveReqVO.setProjectTaskId(noneIfNull(reqDTO.getProjectTaskId()));
        saveReqVO.setPriority(reqDTO.getPriority() == null ? 0 : reqDTO.getPriority());
        LocalDate today = LocalDate.now();
        saveReqVO.setBomId(snapshotService.requireEffectiveBom(reqDTO.getMatId(), today).getId());
        saveReqVO.setRouteId(snapshotService.requireEffectiveRoute(reqDTO.getMatId(), today).getId());
        return saveReqVO;
    }

    private Long noneIfNull(Long id) {
        return id == null ? MesConstants.NONE_ID : id;
    }

}
