package com.iboot.pf.module.wms.dal.mysql.count;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import org.apache.ibatis.annotations.Mapper;

/** 盘点单 Mapper。 */
@Mapper
public interface WmsCountOrderMapper extends BaseMapperX<WmsCountOrderDO> {

    default WmsCountOrderDO selectByCountNo(String countNo) {
        return selectOne(WmsCountOrderDO::getCountNo, countNo);
    }

    /** 按幂等键查盘点单，创建命令判重使用。 */
    default WmsCountOrderDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsCountOrderDO::getIdempotencyKey, idempotencyKey);
    }

}
