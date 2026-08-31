package com.iboot.pf.module.mdm.api.route;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteDetailRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpEsopFileRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpEsopRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import com.iboot.pf.module.mdm.service.esop.RouteOpEsopService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 工艺路线 API 实现类
 *
 * @author iboot
 */
@Service
public class RouteApiImpl implements RouteApi {

    @Resource
    private RouteService routeService;
    @Resource
    private RouteOpEsopService routeOpEsopService;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private FileApi fileApi;
    @Resource
    private RouteOpMapper routeOpMapper;

    @Override
    @DataPermission(enable = false)
    public RouteRespDTO getRoute(Long id) {
        RouteDO route = routeService.getRouteDO(id);
        return BeanUtils.toBean(route, RouteRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public RouteRespDTO getEffectiveRoute(Long matId, LocalDate date) {
        RouteDO route = routeService.getEffectiveRoute(matId, date);
        return BeanUtils.toBean(route, RouteRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public RouteDetailRespDTO getRouteDetail(Long id) {
        RouteDO route = routeService.getRouteDO(id);
        if (route == null) {
            return null;
        }
        RouteDetailRespDTO detail = BeanUtils.toBean(route, RouteDetailRespDTO.class);
        detail.setOps(getRouteOpList(id));
        detail.setMat(materialApi.getMaterial(route.getMatId()));
        return detail;
    }

    @Override
    @DataPermission(enable = false)
    public List<RouteOpRespDTO> getRouteOpList(Long routeId) {
        List<RouteOpDO> ops = routeService.getRouteOpList(routeId);
        return BeanUtils.toBean(ops, RouteOpRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, RouteOpRespDTO> getRouteOpMap(Collection<Long> routeOpIds) {
        if (CollUtil.isEmpty(routeOpIds)) {
            return Collections.emptyMap();
        }
        // 一次 IN 查询，避免调用方 N+1
        List<RouteOpDO> ops = routeOpMapper.selectByIds(routeOpIds);
        return CollectionUtils.convertMap(BeanUtils.toBean(ops, RouteOpRespDTO.class), RouteOpRespDTO::getId);
    }

    @Override
    @DataPermission(enable = false)
    public List<RouteOpMatRespDTO> getRouteOpMatList(Collection<Long> routeOpIds) {
        List<RouteOpMatDO> mats = routeService.getRouteOpMatList(routeOpIds);
        return BeanUtils.toBean(mats, RouteOpMatRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<RouteOpEsopRespDTO> getRouteOpEsopList(Collection<Long> routeOpIds) {
        List<RouteOpEsopDO> esops = routeOpEsopService.getRouteOpEsopList(routeOpIds);
        if (CollUtil.isEmpty(esops)) {
            return new ArrayList<>();
        }
        // 一次查询全部附件并按 esopId 分组，避免 N+1
        List<Long> esopIds = CollectionUtils.convertList(esops, RouteOpEsopDO::getId);
        List<RouteOpEsopFileDO> files = routeOpEsopService.getRouteOpEsopFileList(esopIds);
        Map<Long, List<RouteOpEsopFileDO>> filesByEsop = CollectionUtils.convertMultiMap(files, RouteOpEsopFileDO::getEsopId);

        List<RouteOpEsopRespDTO> result = new ArrayList<>(esops.size());
        for (RouteOpEsopDO esop : esops) {
            RouteOpEsopRespDTO dto = BeanUtils.toBean(esop, RouteOpEsopRespDTO.class);
            dto.setFiles(convertFiles(filesByEsop.getOrDefault(esop.getId(), new ArrayList<>())));
            result.add(dto);
        }
        return result;
    }

    /**
     * 附件转换并对 fileUrl 签发预签名地址，保留 fileName、fileType、fileId。
     */
    private List<RouteOpEsopFileRespDTO> convertFiles(List<RouteOpEsopFileDO> files) {
        if (CollUtil.isEmpty(files)) {
            return new ArrayList<>();
        }
        List<RouteOpEsopFileRespDTO> result = new ArrayList<>(files.size());
        for (RouteOpEsopFileDO file : files) {
            RouteOpEsopFileRespDTO dto = BeanUtils.toBean(file, RouteOpEsopFileRespDTO.class);
            if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                dto.setFileUrl(fileApi.presignGetUrl(file.getFileUrl(), null));
            }
            result.add(dto);
        }
        return result;
    }

}
