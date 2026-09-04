package com.iboot.pf.module.wms.dal.mysql.command;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.wms.dal.dataobject.command.WmsCommandRecordDO;
import org.apache.ibatis.annotations.Mapper;

/** WMS-跨模块命令幂等记录 Mapper。 */
@Mapper
public interface WmsCommandRecordMapper extends BaseMapperX<WmsCommandRecordDO> {

    /** 按幂等键查已受理的命令，唯一索引 uk_wms_command_idempotency_deleted 保证最多一条。 */
    default WmsCommandRecordDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(WmsCommandRecordDO::getIdempotencyKey, idempotencyKey);
    }

}
