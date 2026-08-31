package com.iboot.pf.module.aps.api.projection;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.InventoryProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.MaterialProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionQueryReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.OrderProjectionRespDTO;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import com.iboot.pf.module.aps.service.projection.ApsProjectionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** APS 投影 API 实现。 */
@Service
@Validated
public class ProjectionApiImpl implements ProjectionApi {

    @Resource
    private ApsProjectionService projectionService;

    @Override
    public void refreshMaterialProjection(RefreshProjectionReqDTO reqDTO) {
        projectionService.refreshMaterialProjection(reqDTO);
    }

    @Override
    public void refreshInventoryProjection(RefreshProjectionReqDTO reqDTO) {
        projectionService.refreshInventoryProjection(reqDTO);
    }

    @Override
    public void refreshOrderProjection(RefreshProjectionReqDTO reqDTO) {
        projectionService.refreshOrderProjection(reqDTO);
    }

    @Override
    public void refreshResourceProjection(RefreshProjectionReqDTO reqDTO) {
        projectionService.refreshResourceProjection(reqDTO);
    }

    @Override
    @DataPermission(enable = false)
    public List<MaterialProjectionRespDTO> getMaterialProjectionList(MaterialProjectionQueryReqDTO query) {
        return BeanUtils.toBean(projectionService.getMaterialProjectionList(query), MaterialProjectionRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<InventoryProjectionRespDTO> getInventoryProjectionList(InventoryProjectionQueryReqDTO query) {
        return BeanUtils.toBean(projectionService.getInventoryProjectionList(query), InventoryProjectionRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<OrderProjectionRespDTO> getOrderProjectionList(OrderProjectionQueryReqDTO query) {
        return BeanUtils.toBean(projectionService.getOrderProjectionList(query), OrderProjectionRespDTO.class);
    }

}
