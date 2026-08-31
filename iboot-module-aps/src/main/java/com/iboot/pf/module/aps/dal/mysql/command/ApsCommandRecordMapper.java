package com.iboot.pf.module.aps.dal.mysql.command;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.aps.dal.dataobject.command.ApsCommandRecordDO;
import org.apache.ibatis.annotations.Mapper;

/** APS-跨模块命令幂等记录 Mapper。 */
@Mapper
public interface ApsCommandRecordMapper extends BaseMapperX<ApsCommandRecordDO> {

    /** 按幂等键查已受理的命令，唯一索引 uk_aps_command_idempotency_deleted 保证最多一条。 */
    default ApsCommandRecordDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(ApsCommandRecordDO::getIdempotencyKey, idempotencyKey);
    }

}
