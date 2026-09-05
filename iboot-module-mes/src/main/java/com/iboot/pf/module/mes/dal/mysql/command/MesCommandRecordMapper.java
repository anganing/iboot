package com.iboot.pf.module.mes.dal.mysql.command;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mes.dal.dataobject.command.MesCommandRecordDO;
import org.apache.ibatis.annotations.Mapper;

/** MES-跨模块命令幂等记录 Mapper。 */
@Mapper
public interface MesCommandRecordMapper extends BaseMapperX<MesCommandRecordDO> {

    /** 按幂等键查已受理的命令，唯一索引 uk_mes_command_idempotency_deleted 保证最多一条。 */
    default MesCommandRecordDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(MesCommandRecordDO::getIdempotencyKey, idempotencyKey);
    }

}
