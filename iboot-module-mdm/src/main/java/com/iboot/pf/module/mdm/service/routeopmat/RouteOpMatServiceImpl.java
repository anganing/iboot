package com.iboot.pf.module.mdm.service.routeopmat;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.routeopmat.RouteOpMatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void updateRouteOpMat(RouteOpMatSaveReqVO updateReqVO) {
        RouteOpMatDO existing = validateRouteOpMatExists(updateReqVO.getId());
        updateReqVO.setRouteOpId(existing.getRouteOpId());
        validateReferences(updateReqVO.getRouteOpId(), updateReqVO.getMatId(), updateReqVO.getUomId());
        // 更新
        RouteOpMatDO updateObj = BeanUtils.toBean(updateReqVO, RouteOpMatDO.class);
        routeOpMatMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOpMat(Long id) {
        RouteOpMatDO routeOpMat = validateRouteOpMatExists(id);
        validateRouteEditable(validateRouteOpExists(routeOpMat.getRouteOpId()));
        // 删除
        routeOpMatMapper.deleteById(id);
    }

    @Override
	@Transactional(rollbackFor = Exception.class)
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
        RouteOpDO routeOp = validateRouteOpExists(routeOpId);
        validateRouteEditable(routeOp);
        if (matMapper.selectById(matId) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (uomMapper.selectById(uomId) == null) {
            throw exception(UOM_NOT_EXISTS);
        }
    }

    private RouteOpDO validateRouteOpExists(Long routeOpId) {
        RouteOpDO routeOp = routeOpMapper.selectById(routeOpId);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        return routeOp;
    }

    private void validateRouteEditable(RouteOpDO routeOp) {
        RouteDO route = routeMapper.selectOneForUpdate(RouteDO::getId, routeOp.getRouteId());
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
