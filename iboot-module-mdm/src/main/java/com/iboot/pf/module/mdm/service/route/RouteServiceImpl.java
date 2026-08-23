package com.iboot.pf.module.mdm.service.route;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeop.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 工艺路线 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class RouteServiceImpl implements RouteService {

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
        // 插入
        RouteDO route = BeanUtils.toBean(createReqVO, RouteDO.class);
        route.setId(null);
        routeMapper.insert(route);

        // 返回
        return route.getId();
    }

    @Override
    public void updateRoute(RouteSaveReqVO updateReqVO) {
        RouteDO route = validateRouteExists(updateReqVO.getId());
        validateRouteEditable(route);
        validateRouteFields(updateReqVO);
        if ("PUBLISHED".equals(updateReqVO.getStatus()) && routeOpMapper.selectCountByRouteId(route.getId()) == 0) {
            throw exception(ROUTE_PUBLISH_WITHOUT_OP);
        }
        // 更新
        RouteDO updateObj = BeanUtils.toBean(updateReqVO, RouteDO.class);
        routeMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long id) {
        RouteDO route = validateRouteExists(id);
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


    private RouteDO validateRouteExists(Long id) {
        RouteDO route = routeMapper.selectById(id);
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        return route;
    }

    private void validateRouteFields(RouteSaveReqVO reqVO) {
        if (matMapper.selectById(reqVO.getMatId()) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (!Set.of("DRAFT", "PUBLISHED").contains(reqVO.getStatus())) {
            throw exception(ROUTE_STATUS_INVALID);
        }
        if (reqVO.getExpiryDate() != null && reqVO.getExpiryDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(ROUTE_DATE_INVALID);
        }
    }

    private void validateRouteEditable(RouteDO route) {
        if ("PUBLISHED".equals(route.getStatus())) {
            throw exception(ROUTE_PUBLISHED_LOCKED);
        }
    }

    @Override
    public RouteDO getRoute(Long id) {
        return routeMapper.selectById(id);
    }

    @Override
    public PageResult<RouteDO> getRoutePage(RoutePageReqVO pageReqVO) {
        return routeMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（工艺路线工序） ====================

    @Override
    public PageResult<RouteOpDO> getRouteOpPage(PageParam pageReqVO, Long routeId) {
        return routeOpMapper.selectPage(pageReqVO, routeId);
    }

    @Override
    public Long createRouteOp(RouteOpDO routeOp) {
        RouteDO route = validateRouteExists(routeOp.getRouteId());
        validateRouteEditable(route);
        validateRouteOpReferences(routeOp);
        routeOp.clean(); // 清理掉创建、更新时间等相关属性值
        routeOp.setId(null);
        routeOpMapper.insert(routeOp);
        return routeOp.getId();
    }

    @Override
    public void updateRouteOp(RouteOpDO routeOp) {
        RouteOpDO existing = validateRouteOpExists(routeOp.getId());
        validateRouteEditable(validateRouteExists(existing.getRouteId()));
        routeOp.setRouteId(existing.getRouteId());
        validateRouteOpReferences(routeOp);
        // 更新
        routeOp.clean(); // 解决更新情况下：updateTime 不更新
        routeOpMapper.updateById(routeOp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOp(Long id) {
        RouteOpDO routeOp = validateRouteOpExists(id);
        validateRouteEditable(validateRouteExists(routeOp.getRouteId()));
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

    private RouteOpDO validateRouteOpExists(Long id) {
        RouteOpDO routeOp = routeOpMapper.selectById(id);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        return routeOp;
    }

    private void validateRouteOpReferences(RouteOpDO routeOp) {
        if (opMapper.selectById(routeOp.getOpId()) == null) {
            throw exception(OP_NOT_EXISTS);
        }
        if (wcMapper.selectById(routeOp.getWcId()) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    private void deleteRouteOpByRouteId(Long routeId) {
        List<Long> routeOpIds = convertList(routeOpMapper.selectList(RouteOpDO::getRouteId, routeId), RouteOpDO::getId);
        if (!routeOpIds.isEmpty()) {
            routeOpMatMapper.deleteByRouteOpIds(routeOpIds);
        }
        routeOpMapper.deleteByRouteId(routeId);
    }

	private void deleteRouteOpByRouteIds(List<Long> routeIds) {
        routeOpMapper.deleteByRouteIds(routeIds);
	}

}
