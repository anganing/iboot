package com.iboot.pf.module.wms.dal.mysql.container;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerDO;
import org.apache.ibatis.annotations.Mapper;

/** 容器主档 Mapper。 */
@Mapper
public interface WmsContainerMapper extends BaseMapperX<WmsContainerDO> {
}
