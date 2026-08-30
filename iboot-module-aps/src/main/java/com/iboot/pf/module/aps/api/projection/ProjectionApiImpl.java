package com.iboot.pf.module.aps.api.projection;
import com.iboot.pf.framework.common.util.object.BeanUtils; import com.iboot.pf.framework.datapermission.core.annotation.DataPermission; import com.iboot.pf.module.aps.api.projection.dto.*; import com.iboot.pf.module.aps.service.projection.ApsProjectionService; import jakarta.annotation.Resource; import org.springframework.stereotype.Service; import org.springframework.validation.annotation.Validated; import java.util.*;
/** APS 投影 API 实现。 */ @Service @Validated
public class ProjectionApiImpl implements ProjectionApi { @Resource private ApsProjectionService service;
 public void refreshMaterialProjection(RefreshProjectionReqDTO r){service.refreshMaterialProjection(r);} public void refreshInventoryProjection(RefreshProjectionReqDTO r){service.refreshInventoryProjection(r);} public void refreshOrderProjection(RefreshProjectionReqDTO r){service.refreshOrderProjection(r);}
 @DataPermission(enable=false) public List<MaterialProjectionRespDTO> getMaterialProjectionList(MaterialProjectionQueryReqDTO q){return BeanUtils.toBean(service.getMaterialProjectionList(q),MaterialProjectionRespDTO.class);}
 @DataPermission(enable=false) public List<InventoryProjectionRespDTO> getInventoryProjectionList(InventoryProjectionQueryReqDTO q){return BeanUtils.toBean(service.getInventoryProjectionList(q),InventoryProjectionRespDTO.class);}
 @DataPermission(enable=false) public List<OrderProjectionRespDTO> getOrderProjectionList(OrderProjectionQueryReqDTO q){return BeanUtils.toBean(service.getOrderProjectionList(q),OrderProjectionRespDTO.class);} }
