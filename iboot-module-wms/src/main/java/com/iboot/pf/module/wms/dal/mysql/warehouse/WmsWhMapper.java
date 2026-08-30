package com.iboot.pf.module.wms.dal.mysql.warehouse;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import org.apache.ibatis.annotations.Mapper;

/** 仓库 Mapper。 */
@Mapper
public interface WmsWhMapper extends BaseMapperX<WmsWhDO> {
}
