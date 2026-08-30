package com.iboot.pf.module.mdm.service.tool;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.tool.RouteOpToolDO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.ToolDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.tool.RouteOpToolMapper;
import com.iboot.pf.module.mdm.dal.mysql.tool.ToolMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.TOOL;

/**
 * 工装模具刀具 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ToolServiceImpl implements ToolService {

    @Resource
    private ToolMapper toolMapper;
    @Resource
    private RouteOpToolMapper routeOpToolMapper;
    @Resource
    private WcMapper wcMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    // ==================== 工装 ====================

    @Override
    public Long createTool(ToolSaveReqVO createReqVO) {
        validateWcExists(createReqVO.getWcId());
        ToolDO tool = BeanUtils.toBean(createReqVO, ToolDO.class);
        tool.setId(null);
        tool.setCode(mdmCodeService.resolveCode(tool.getCode(), TOOL));
        toolMapper.insert(tool);
        return tool.getId();
    }

    @Override
    public void updateTool(ToolSaveReqVO updateReqVO) {
        validateToolExists(updateReqVO.getId());
        validateWcExists(updateReqVO.getWcId());
        ToolDO updateObj = BeanUtils.toBean(updateReqVO, ToolDO.class);
        toolMapper.updateById(updateObj);
    }

    @Override
    public void deleteTool(Long id) {
        validateToolExists(id);
        if (!routeOpToolMapper.selectList(RouteOpToolDO::getToolId, id).isEmpty()) {
            throw exception(TOOL_DELETE_REFERENCED);
        }
        toolMapper.deleteById(id);
    }

    @Override
    public void deleteToolListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteTool(id);
        }
    }

    private ToolDO validateToolExists(Long id) {
        ToolDO tool = toolMapper.selectById(id);
        if (tool == null) {
            throw exception(TOOL_NOT_EXISTS);
        }
        return tool;
    }

    private void validateWcExists(Long wcId) {
        if (wcId == null) {
            return;
        }
        WcDO wc = wcMapper.selectById(wcId);
        if (wc == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    @Override
    public ToolRespVO getTool(Long id) {
        return toolMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ToolRespVO> getToolPage(ToolPageReqVO pageReqVO) {
        return toolMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public List<ToolDO> getToolList() {
        return toolMapper.selectList();
    }

    // ==================== 工序工装 ====================

    @Override
    public Long createRouteOpTool(RouteOpToolSaveReqVO createReqVO) {
        validateToolExists(createReqVO.getToolId());
        validateRouteOpToolUnique(createReqVO.getRouteOpId(), createReqVO.getToolId(), null);
        RouteOpToolDO routeOpTool = BeanUtils.toBean(createReqVO, RouteOpToolDO.class);
        routeOpTool.setId(null);
        routeOpToolMapper.insert(routeOpTool);
        return routeOpTool.getId();
    }

    @Override
    public void updateRouteOpTool(RouteOpToolSaveReqVO updateReqVO) {
        validateRouteOpToolExists(updateReqVO.getId());
        validateToolExists(updateReqVO.getToolId());
        validateRouteOpToolUnique(updateReqVO.getRouteOpId(), updateReqVO.getToolId(), updateReqVO.getId());
        routeOpToolMapper.updateById(BeanUtils.toBean(updateReqVO, RouteOpToolDO.class));
    }

    @Override
    public void deleteRouteOpTool(Long id) {
        validateRouteOpToolExists(id);
        routeOpToolMapper.deleteById(id);
    }

    @Override
    public void deleteRouteOpToolListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteRouteOpTool(id);
        }
    }

    private void validateRouteOpToolExists(Long id) {
        if (routeOpToolMapper.selectById(id) == null) {
            throw exception(ROUTE_OP_TOOL_NOT_EXISTS);
        }
    }

    private void validateRouteOpToolUnique(Long routeOpId, Long toolId, Long excludeId) {
        RouteOpToolDO existing = routeOpToolMapper.selectByRouteOpIdAndToolId(routeOpId, toolId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(ROUTE_OP_TOOL_DUPLICATE);
        }
    }

    @Override
    public RouteOpToolRespVO getRouteOpTool(Long id) {
        return routeOpToolMapper.selectJoinById(id);
    }

    @Override
    public PageResult<RouteOpToolRespVO> getRouteOpToolPage(PageParam pageParam, Long routeOpId) {
        return routeOpToolMapper.selectJoinPage(pageParam, routeOpId);
    }

    @Override
    public List<RouteOpToolDO> getRouteOpToolListByRouteOpId(Long routeOpId) {
        return routeOpToolMapper.selectListByRouteOpId(routeOpId);
    }

    @Override
    public List<RouteOpToolDO> getRouteOpToolList(Collection<Long> routeOpIds) {
        if (routeOpIds == null || routeOpIds.isEmpty()) {
            return List.of();
        }
        return routeOpToolMapper.selectListByRouteOpIds(List.copyOf(routeOpIds));
    }

}
