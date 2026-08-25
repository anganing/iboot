package com.iboot.pf.module.mdm.service.route;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/** 工艺路线 Service 实现类。 */
@Service
@Validated
public class RouteServiceImpl implements RouteService {

    private static final int ROUTE_OP_SEQ_STEP = 10;

    @Resource
    private RouteMapper routeMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
    @Resource
    private RouteOpMatMapper routeOpMatMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private WcMapper wcMapper;
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long createRoute(RouteSaveReqVO createReqVO) {
        validateRouteFields(createReqVO);
        RouteDO route = BeanUtils.toBean(createReqVO, RouteDO.class);
        route.setId(null);
        routeMapper.insert(route);
        return route.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoute(RouteSaveReqVO updateReqVO) {
        RouteDO route = validateRouteExistsForUpdate(updateReqVO.getId());
        validateRouteEditable(route);
        validateRouteFields(updateReqVO);
        if ("PUBLISHED".equals(updateReqVO.getStatus())
                && routeOpMapper.selectCountByRouteId(route.getId()) == 0) {
            throw exception(ROUTE_PUBLISH_WITHOUT_OP);
        }
        routeMapper.updateById(BeanUtils.toBean(updateReqVO, RouteDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        RouteDO route = validateRouteExistsForUpdate(id);
        validateRouteEditable(route);
        deleteRouteOpByRouteId(id);
        routeMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteRoute(id);
        }
    }

    @Override
    public RouteRespVO getRoute(Long id) {
        RouteDO route = routeMapper.selectById(id);
        if (route == null) {
            return null;
        }
        return buildRouteResponse(route, getMaterialMap(List.of(route.getMatId())));
    }

    @Override
    public PageResult<RouteRespVO> getRoutePage(RoutePageReqVO pageReqVO) {
        PageResult<RouteDO> pageResult = routeMapper.selectPage(pageReqVO);
        Map<Long, MatDO> materialMap = getMaterialMap(
                convertList(pageResult.getList(), RouteDO::getMatId));
        List<RouteRespVO> list = convertList(pageResult.getList(),
                route -> buildRouteResponse(route, materialMap));
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public RouteDetailRespVO getRouteDetail(Long id) {
        RouteDO route = validateRouteExists(id);
        List<RouteOpDO> routeOps = routeOpMapper.selectListByRouteId(id);
        List<Long> routeOpIds = convertList(routeOps, RouteOpDO::getId);
        List<RouteOpMatDO> routeOpMaterials = routeOpMatMapper.selectListByRouteOpIds(routeOpIds);

        Set<Long> materialIds = routeOpMaterials.stream()
                .map(RouteOpMatDO::getMatId)
                .collect(Collectors.toSet());
        materialIds.add(route.getMatId());
        Map<Long, MatDO> materialMap = getMaterialMap(materialIds);
        Map<Long, OpDO> operationMap = getEntityMap(routeOps, RouteOpDO::getOpId,
                ids -> opMapper.selectByIds(ids), OpDO::getId);
        Map<Long, WcDO> workCenterMap = getEntityMap(routeOps, RouteOpDO::getWcId,
                ids -> wcMapper.selectByIds(ids), WcDO::getId);
        Map<Long, UomDO> uomMap = getEntityMap(routeOpMaterials, RouteOpMatDO::getUomId,
                ids -> uomMapper.selectByIds(ids), UomDO::getId);
        Map<Long, List<RouteOpMatDO>> materialsByRouteOp = routeOpMaterials.stream()
                .collect(Collectors.groupingBy(RouteOpMatDO::getRouteOpId));

        RouteDetailRespVO detail = BeanUtils.toBean(
                buildRouteResponse(route, materialMap), RouteDetailRespVO.class);
        detail.setOperations(convertList(routeOps, routeOp -> buildRouteOpResponse(
                routeOp, operationMap, workCenterMap, materialMap, uomMap,
                materialsByRouteOp.getOrDefault(routeOp.getId(), Collections.emptyList()))));
        return detail;
    }

    @Override
    public PageResult<RouteOpDO> getRouteOpPage(PageParam pageReqVO, Long routeId) {
        return routeOpMapper.selectPage(pageReqVO, routeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRouteOp(RouteOpSaveReqVO saveReqVO) {
        RouteDO route = validateRouteExistsForUpdate(saveReqVO.getRouteId());
        validateRouteEditable(route);
        validateRouteOpReferences(saveReqVO.getOpId(), saveReqVO.getWcId());

        Integer maxSeq = routeOpMapper.selectMaxSeqByRouteId(route.getId());
        RouteOpDO routeOp = BeanUtils.toBean(saveReqVO, RouteOpDO.class);
        routeOp.setId(null);
        routeOp.setSeq(maxSeq == null ? ROUTE_OP_SEQ_STEP : maxSeq + ROUTE_OP_SEQ_STEP);
        routeOp.clean();
        routeOpMapper.insert(routeOp);
        return routeOp.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRouteOp(RouteOpSaveReqVO saveReqVO) {
        RouteOpDO existing = validateRouteOpExists(saveReqVO.getId());
        RouteDO route = validateRouteExistsForUpdate(existing.getRouteId());
        validateRouteEditable(route);
        validateRouteOpReferences(saveReqVO.getOpId(), saveReqVO.getWcId());

        RouteOpDO updateObj = BeanUtils.toBean(saveReqVO, RouteOpDO.class);
        updateObj.setRouteId(existing.getRouteId());
        updateObj.setSeq(existing.getSeq());
        updateObj.clean();
        routeOpMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRouteOpSort(RouteOpSortReqVO sortReqVO) {
        RouteDO route = validateRouteExistsForUpdate(sortReqVO.getRouteId());
        validateRouteEditable(route);
        List<RouteOpDO> routeOps = routeOpMapper.selectListByRouteId(route.getId());
        List<Long> actualIds = convertList(routeOps, RouteOpDO::getId);
        List<Long> orderedIds = sortReqVO.getOrderedIds();
        if (orderedIds.size() != actualIds.size()
                || new HashSet<>(orderedIds).size() != orderedIds.size()
                || !new HashSet<>(orderedIds).equals(new HashSet<>(actualIds))) {
            throw exception(ROUTE_OP_SORT_INVALID);
        }

        List<RouteOpDO> temporaryUpdates = IntStream.range(0, orderedIds.size())
                .mapToObj(index -> new RouteOpDO().setId(orderedIds.get(index))
                        .setSeq(Integer.MIN_VALUE + index))
                .toList();
        routeOpMapper.updateBatch(temporaryUpdates);

        List<RouteOpDO> finalUpdates = IntStream.range(0, orderedIds.size())
                .mapToObj(index -> new RouteOpDO().setId(orderedIds.get(index))
                        .setSeq((index + 1) * ROUTE_OP_SEQ_STEP))
                .toList();
        routeOpMapper.updateBatch(finalUpdates);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOp(Long id) {
        RouteOpDO routeOp = validateRouteOpExists(id);
        RouteDO route = validateRouteExistsForUpdate(routeOp.getRouteId());
        validateRouteEditable(route);
        routeOpMatMapper.deleteByRouteOpIds(List.of(id));
        routeOpMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOpListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteRouteOp(id);
        }
    }

    @Override
    public RouteOpDO getRouteOp(Long id) {
        return routeOpMapper.selectById(id);
    }

    private RouteDO validateRouteExists(Long id) {
        RouteDO route = routeMapper.selectById(id);
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        return route;
    }

    private RouteDO validateRouteExistsForUpdate(Long id) {
        RouteDO route = routeMapper.selectOneForUpdate(RouteDO::getId, id);
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        return route;
    }

    private RouteOpDO validateRouteOpExists(Long id) {
        RouteOpDO routeOp = routeOpMapper.selectById(id);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        return routeOp;
    }

    private void validateRouteFields(RouteSaveReqVO reqVO) {
        if (matMapper.selectById(reqVO.getMatId()) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (!Set.of("DRAFT", "PUBLISHED").contains(reqVO.getStatus())) {
            throw exception(ROUTE_STATUS_INVALID);
        }
        if (reqVO.getExpiryDate() != null
                && reqVO.getExpiryDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(ROUTE_DATE_INVALID);
        }
    }

    private void validateRouteEditable(RouteDO route) {
        if ("PUBLISHED".equals(route.getStatus())) {
            throw exception(ROUTE_PUBLISHED_LOCKED);
        }
    }

    private void validateRouteOpReferences(Long opId, Long wcId) {
        if (opMapper.selectById(opId) == null) {
            throw exception(OP_NOT_EXISTS);
        }
        if (wcMapper.selectById(wcId) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    private void deleteRouteOpByRouteId(Long routeId) {
        List<Long> routeOpIds = convertList(
                routeOpMapper.selectListByRouteId(routeId), RouteOpDO::getId);
        if (!routeOpIds.isEmpty()) {
            routeOpMatMapper.deleteByRouteOpIds(routeOpIds);
        }
        routeOpMapper.deleteByRouteId(routeId);
    }

    private RouteRespVO buildRouteResponse(RouteDO route, Map<Long, MatDO> materialMap) {
        RouteRespVO response = BeanUtils.toBean(route, RouteRespVO.class);
        MatDO material = materialMap.get(route.getMatId());
        if (material != null) {
            response.setMatCode(material.getCode());
            response.setMatName(material.getName());
        }
        return response;
    }

    private RouteOpRespVO buildRouteOpResponse(RouteOpDO routeOp,
                                                Map<Long, OpDO> operationMap,
                                                Map<Long, WcDO> workCenterMap,
                                                Map<Long, MatDO> materialMap,
                                                Map<Long, UomDO> uomMap,
                                                List<RouteOpMatDO> materials) {
        RouteOpRespVO response = BeanUtils.toBean(routeOp, RouteOpRespVO.class);
        OpDO operation = operationMap.get(routeOp.getOpId());
        if (operation != null) {
            response.setOpCode(operation.getCode());
            response.setOpName(operation.getName());
        }
        WcDO workCenter = workCenterMap.get(routeOp.getWcId());
        if (workCenter != null) {
            response.setWcCode(workCenter.getCode());
            response.setWcName(workCenter.getName());
        }
        response.setMaterialCount(materials.size());
        response.setMaterials(convertList(materials,
                material -> buildRouteOpMaterialResponse(material, materialMap, uomMap)));
        return response;
    }

    private RouteOpMatRespVO buildRouteOpMaterialResponse(RouteOpMatDO routeOpMaterial,
                                                           Map<Long, MatDO> materialMap,
                                                           Map<Long, UomDO> uomMap) {
        RouteOpMatRespVO response = BeanUtils.toBean(routeOpMaterial, RouteOpMatRespVO.class);
        MatDO material = materialMap.get(routeOpMaterial.getMatId());
        if (material != null) {
            response.setMatCode(material.getCode());
            response.setMatName(material.getName());
            response.setMatSpec(material.getSpec());
        }
        UomDO uom = uomMap.get(routeOpMaterial.getUomId());
        if (uom != null) {
            response.setUomCode(uom.getCode());
            response.setUomName(uom.getName());
        }
        return response;
    }

    private Map<Long, MatDO> getMaterialMap(Collection<Long> materialIds) {
        Set<Long> ids = materialIds.stream().filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return convertMap(matMapper.selectByIds(ids), MatDO::getId);
    }

    private <S, E> Map<Long, E> getEntityMap(Collection<S> sources,
                                              Function<S, Long> idMapper,
                                              Function<Collection<Long>, List<E>> query,
                                              Function<E, Long> entityIdMapper) {
        Set<Long> ids = sources.stream().map(idMapper).filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return convertMap(query.apply(ids), entityIdMapper);
    }

}
