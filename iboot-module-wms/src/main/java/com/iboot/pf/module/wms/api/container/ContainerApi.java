package com.iboot.pf.module.wms.api.container;

import com.iboot.pf.module.wms.api.container.dto.ContainerRespDTO;
import com.iboot.pf.module.wms.api.container.dto.ContainerTypeRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 容器 API 门面：容器状态与位置查询。
 *
 * <p>向 MES / Integration 提供容器主档与容器类型的只读查询。
 */
public interface ContainerApi {

    ContainerRespDTO getContainer(Long id);

    List<ContainerRespDTO> getContainerList(Collection<Long> ids);

    Map<Long, ContainerRespDTO> getContainerMap(Collection<Long> ids);

    ContainerTypeRespDTO getContainerType(Long id);

    List<ContainerTypeRespDTO> getContainerTypeList(Collection<Long> ids);

    Map<Long, ContainerTypeRespDTO> getContainerTypeMap(Collection<Long> ids);

}
