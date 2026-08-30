package com.iboot.pf.module.wms.service.container;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerDO;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerTypeDO;
import com.iboot.pf.module.wms.dal.mysql.container.WmsContainerMapper;
import com.iboot.pf.module.wms.dal.mysql.container.WmsContainerTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_NOT_EXISTS;

/** 容器 Service 实现。 */
@Service
public class WmsContainerServiceImpl implements WmsContainerService {

    @Resource
    private WmsContainerMapper wmsContainerMapper;
    @Resource
    private WmsContainerTypeMapper wmsContainerTypeMapper;

    @Override
    public WmsContainerDO getContainer(Long id) {
        return wmsContainerMapper.selectById(id);
    }

    @Override
    public List<WmsContainerDO> getContainerList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsContainerMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsContainerDO> getContainerMap(Collection<Long> ids) {
        return convertMap(getContainerList(ids), WmsContainerDO::getId);
    }

    @Override
    public WmsContainerTypeDO getContainerType(Long id) {
        return wmsContainerTypeMapper.selectById(id);
    }

    @Override
    public List<WmsContainerTypeDO> getContainerTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return wmsContainerTypeMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, WmsContainerTypeDO> getContainerTypeMap(Collection<Long> ids) {
        return convertMap(getContainerTypeList(ids), WmsContainerTypeDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveContainer(Long containerId, Long locId) {
        WmsContainerDO container = wmsContainerMapper.selectById(containerId);
        if (container == null) {
            throw exception(CONTAINER_NOT_EXISTS);
        }
        container.setLocId(locId);
        wmsContainerMapper.updateById(container);
    }

}
