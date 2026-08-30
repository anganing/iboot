package com.iboot.pf.module.wms.service.container;

import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerDO;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerTypeDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 容器 Service。 */
public interface WmsContainerService {

    WmsContainerDO getContainer(Long id);

    List<WmsContainerDO> getContainerList(Collection<Long> ids);

    Map<Long, WmsContainerDO> getContainerMap(Collection<Long> ids);

    WmsContainerTypeDO getContainerType(Long id);

    List<WmsContainerTypeDO> getContainerTypeList(Collection<Long> ids);

    Map<Long, WmsContainerTypeDO> getContainerTypeMap(Collection<Long> ids);

    /** 更新容器当前库位。 */
    void moveContainer(Long containerId, Long locId);

}
