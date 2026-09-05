package com.iboot.pf.module.wms.api.container;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.wms.api.container.dto.ContainerRespDTO;
import com.iboot.pf.module.wms.api.container.dto.ContainerTypeRespDTO;
import com.iboot.pf.module.wms.service.container.WmsContainerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 容器 API 实现类。
 *
 * <p>查询方法关闭数据权限，避免内部跨模块调用被当前用户数据权限过滤。
 */
@Service
@Validated
@DataPermission(enable = false)
public class ContainerApiImpl implements ContainerApi {

    @Resource
    private WmsContainerService containerService;

    @Override
    public ContainerRespDTO getContainer(Long id) {
        return BeanUtils.toBean(containerService.getContainer(id), ContainerRespDTO.class);
    }

    @Override
    public List<ContainerRespDTO> getContainerList(Collection<Long> ids) {
        return BeanUtils.toBean(containerService.getContainerList(ids), ContainerRespDTO.class);
    }

    @Override
    public Map<Long, ContainerRespDTO> getContainerMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(containerService.getContainerList(ids), ContainerRespDTO.class),
                ContainerRespDTO::getId);
    }

    @Override
    public ContainerTypeRespDTO getContainerType(Long id) {
        return BeanUtils.toBean(containerService.getContainerType(id), ContainerTypeRespDTO.class);
    }

    @Override
    public List<ContainerTypeRespDTO> getContainerTypeList(Collection<Long> ids) {
        return BeanUtils.toBean(containerService.getContainerTypeList(ids), ContainerTypeRespDTO.class);
    }

    @Override
    public Map<Long, ContainerTypeRespDTO> getContainerTypeMap(Collection<Long> ids) {
        return convertMap(BeanUtils.toBean(containerService.getContainerTypeList(ids), ContainerTypeRespDTO.class),
                ContainerTypeRespDTO::getId);
    }

}
