package com.iboot.pf.module.mdm.service.route;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
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
        return routeMapper.selectJoinById(id);
    }

    @Override
    public PageResult<RouteRespVO> getRoutePage(RoutePageReqVO pageReqVO) {
        return routeMapper.selectPage(pageReqVO);
    }

    @Override
    public RouteDetailRespVO getRouteDetail(Long id) {
        validateRouteExists(id);
        return routeMapper.selectDetailById(id);
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

}
