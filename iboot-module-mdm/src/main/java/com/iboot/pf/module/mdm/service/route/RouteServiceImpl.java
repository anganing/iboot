package com.iboot.pf.module.mdm.service.route;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopFileMapper;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.tool.RouteOpToolMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
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
    private RouteOpEsopMapper routeOpEsopMapper;
    @Resource
    private RouteOpEsopFileMapper routeOpEsopFileMapper;
    @Resource
    private RouteOpToolMapper routeOpToolMapper;
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
        if (CommonStatusEnum.isEnable(updateReqVO.getStatus())
                && routeOpMapper.selectCountByRouteId(route.getId()) == 0) {
            throw exception(ROUTE_ENABLE_WITHOUT_OP);
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
        deleteEsopByRouteOpIds(List.of(id));
        routeOpToolMapper.deleteByRouteOpIds(List.of(id));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyRoute(RouteCopyReqVO copyReqVO) {
        RouteDO source = validateRouteExistsForUpdate(copyReqVO.getId());
        if (routeMapper.selectByMatIdAndVersion(source.getMatId(), copyReqVO.getVersion()) != null) {
            throw exception(ROUTE_VERSION_DUPLICATE);
        }

        // 复制路线头：新版本为停用状态，待编辑完成后再启用
        RouteDO newRoute = new RouteDO();
        newRoute.setMatId(source.getMatId());
        newRoute.setVersion(copyReqVO.getVersion());
        newRoute.setStatus(CommonStatusEnum.DISABLE.getStatus());
        newRoute.setEffectiveDate(copyReqVO.getEffectiveDate() != null
                ? copyReqVO.getEffectiveDate() : LocalDate.now());
        newRoute.setRemark(source.getRemark());
        routeMapper.insert(newRoute);

        // 复制工序并建立新旧工序映射
        List<RouteOpDO> sourceOps = routeOpMapper.selectListByRouteId(source.getId());
        Map<Long, Long> routeOpIdMapping = new HashMap<>();
        for (RouteOpDO sourceOp : sourceOps) {
            RouteOpDO newOp = BeanUtils.toBean(sourceOp, RouteOpDO.class);
            newOp.setId(null);
            newOp.setRouteId(newRoute.getId());
            routeOpMapper.insert(newOp);
            routeOpIdMapping.put(sourceOp.getId(), newOp.getId());
        }

        // 复制工序用料
        if (!sourceOps.isEmpty()) {
            List<Long> sourceOpIds = convertList(sourceOps, RouteOpDO::getId);
            List<RouteOpMatDO> sourceMats = routeOpMatMapper.selectListByRouteOpIds(sourceOpIds);
            if (!sourceMats.isEmpty()) {
                List<RouteOpMatDO> newMats = sourceMats.stream().map(mat -> {
                    RouteOpMatDO newMat = BeanUtils.toBean(mat, RouteOpMatDO.class);
                    newMat.setId(null);
                    newMat.setRouteOpId(routeOpIdMapping.get(mat.getRouteOpId()));
                    return newMat;
                }).toList();
                routeOpMatMapper.insertBatch(newMats);
            }

            // 复制 ESOP 作业步骤：route_op_id 重指向新版本工序，改版无需重新挂载文档
            List<RouteOpEsopDO> sourceEsops = routeOpEsopMapper.selectListByRouteOpIds(sourceOpIds);
            if (!sourceEsops.isEmpty()) {
                Map<Long, Long> esopIdMapping = new HashMap<>();
                for (RouteOpEsopDO sourceEsop : sourceEsops) {
                    RouteOpEsopDO newEsop = BeanUtils.toBean(sourceEsop, RouteOpEsopDO.class);
                    newEsop.setId(null);
                    newEsop.setRouteOpId(routeOpIdMapping.get(sourceEsop.getRouteOpId()));
                    routeOpEsopMapper.insert(newEsop);
                    esopIdMapping.put(sourceEsop.getId(), newEsop.getId());
                }
                // 复制作业步骤附件：esop_id 重指向新建的作业步骤
                List<RouteOpEsopFileDO> sourceFiles =
                        routeOpEsopFileMapper.selectListByEsopIds(new ArrayList<>(esopIdMapping.keySet()));
                if (!sourceFiles.isEmpty()) {
                    List<RouteOpEsopFileDO> newFiles = sourceFiles.stream().map(file -> {
                        RouteOpEsopFileDO newFile = BeanUtils.toBean(file, RouteOpEsopFileDO.class);
                        newFile.setId(null);
                        newFile.setEsopId(esopIdMapping.get(file.getEsopId()));
                        return newFile;
                    }).toList();
                    routeOpEsopFileMapper.insertBatch(newFiles);
                }
            }
        }
        return newRoute.getId();
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
        if (!CommonStatusEnum.isEnable(reqVO.getStatus()) && !CommonStatusEnum.isDisable(reqVO.getStatus())) {
            throw exception(ROUTE_STATUS_INVALID);
        }
        if (reqVO.getExpiryDate() != null
                && reqVO.getExpiryDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(ROUTE_DATE_INVALID);
        }
    }

    private void validateRouteEditable(RouteDO route) {
        if (CommonStatusEnum.isEnable(route.getStatus())) {
            throw exception(ROUTE_ENABLED_LOCKED);
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
            deleteEsopByRouteOpIds(routeOpIds);
            routeOpToolMapper.deleteByRouteOpIds(routeOpIds);
        }
        routeOpMapper.deleteByRouteId(routeId);
    }

    /**
     * 工序删除时级联清理作业步骤及其附件，避免残留孤儿数据。
     */
    private void deleteEsopByRouteOpIds(List<Long> routeOpIds) {
        List<Long> esopIds = convertList(
                routeOpEsopMapper.selectListByRouteOpIds(routeOpIds), RouteOpEsopDO::getId);
        if (!esopIds.isEmpty()) {
            routeOpEsopFileMapper.deleteByEsopIds(esopIds);
        }
        routeOpEsopMapper.deleteByRouteOpIds(routeOpIds);
    }

    @Override
    public RouteDO getRouteDO(Long id) {
        return routeMapper.selectById(id);
    }

    @Override
    public RouteDO getEffectiveRoute(Long matId, LocalDate date) {
        // 仅启用状态；effectiveDate <= date，且 expiryDate 为空（开放式）或 >= date。
        // 命中多个时取 effectiveDate 最新一条。小数据量内存过滤，规避 expiryDate 为空时 SQL 误伤。
        List<RouteDO> routes = routeMapper.selectList(RouteDO::getMatId, matId);
        RouteDO best = null;
        for (RouteDO route : routes) {
            if (!CommonStatusEnum.isEnable(route.getStatus())) {
                continue;
            }
            if (route.getEffectiveDate() != null && route.getEffectiveDate().isAfter(date)) {
                continue;
            }
            if (route.getExpiryDate() != null && route.getExpiryDate().isBefore(date)) {
                continue;
            }
            if (best == null || route.getEffectiveDate().isAfter(best.getEffectiveDate())) {
                best = route;
            }
        }
        return best;
    }

    @Override
    public List<RouteOpDO> getRouteOpList(Long routeId) {
        return routeOpMapper.selectListByRouteId(routeId);
    }

    @Override
    public List<RouteOpMatDO> getRouteOpMatList(Collection<Long> routeOpIds) {
        if (routeOpIds == null || routeOpIds.isEmpty()) {
            return List.of();
        }
        return routeOpMatMapper.selectListByRouteOpIds(new ArrayList<>(routeOpIds));
    }

}
