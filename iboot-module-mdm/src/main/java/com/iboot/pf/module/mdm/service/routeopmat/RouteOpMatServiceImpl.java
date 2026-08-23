package com.iboot.pf.module.mdm.service.routeopmat;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeop.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 工序用料 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class RouteOpMatServiceImpl implements RouteOpMatService {

    @Resource
    private RouteOpMatMapper routeOpMatMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
    @Resource
    private RouteMapper routeMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long createRouteOpMat(RouteOpMatSaveReqVO createReqVO) {
        validateReferences(createReqVO.getRouteOpId(), createReqVO.getMatId(), createReqVO.getUomId());
        // 插入
        RouteOpMatDO routeOpMat = BeanUtils.toBean(createReqVO, RouteOpMatDO.class);
        routeOpMat.setId(null);
        routeOpMatMapper.insert(routeOpMat);

        // 返回
        return routeOpMat.getId();
    }

    @Override
    public void updateRouteOpMat(RouteOpMatSaveReqVO updateReqVO) {
        RouteOpMatDO existing = validateRouteOpMatExists(updateReqVO.getId());
        updateReqVO.setRouteOpId(existing.getRouteOpId());
        validateReferences(updateReqVO.getRouteOpId(), updateReqVO.getMatId(), updateReqVO.getUomId());
        // 更新
        RouteOpMatDO updateObj = BeanUtils.toBean(updateReqVO, RouteOpMatDO.class);
        routeOpMatMapper.updateById(updateObj);
    }

    @Override
    public void deleteRouteOpMat(Long id) {
        RouteOpMatDO routeOpMat = validateRouteOpMatExists(id);
        validateRouteEditable(routeOpMat.getRouteOpId());
        // 删除
        routeOpMatMapper.deleteById(id);
    }

    @Override
	public void deleteRouteOpMatListByIds(List<Long> ids) {
	    for (Long id : ids) {
            deleteRouteOpMat(id);
        }
        }


    private RouteOpMatDO validateRouteOpMatExists(Long id) {
        RouteOpMatDO routeOpMat = routeOpMatMapper.selectById(id);
        if (routeOpMat == null) {
            throw exception(ROUTE_OP_MAT_NOT_EXISTS);
        }
        return routeOpMat;
    }

    private void validateReferences(Long routeOpId, Long matId, Long uomId) {
        RouteOpDO routeOp = routeOpMapper.selectById(routeOpId);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        validateRouteEditable(routeOpId);
        if (matMapper.selectById(matId) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (uomMapper.selectById(uomId) == null) {
            throw exception(UOM_NOT_EXISTS);
        }
    }

    private void validateRouteEditable(Long routeOpId) {
        RouteOpDO routeOp = routeOpMapper.selectById(routeOpId);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        RouteDO route = routeMapper.selectById(routeOp.getRouteId());
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        if ("PUBLISHED".equals(route.getStatus())) {
            throw exception(ROUTE_PUBLISHED_LOCKED);
        }
    }

    @Override
    public RouteOpMatDO getRouteOpMat(Long id) {
        return routeOpMatMapper.selectById(id);
    }

    @Override
    public PageResult<RouteOpMatDO> getRouteOpMatPage(RouteOpMatPageReqVO pageReqVO) {
        return routeOpMatMapper.selectPage(pageReqVO);
    }

}
