package com.iboot.pf.module.wms.service.command;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import com.iboot.pf.module.wms.dal.dataobject.command.WmsCommandRecordDO;
import com.iboot.pf.module.wms.dal.mysql.command.WmsCommandRecordMapper;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * WMS 跨模块命令幂等服务实现。
 *
 * <p>幂等记录与业务动作在同一个事务里落库：动作失败时记录一起回滚，重试才能重新执行。
 * 如果先记录再执行、或者两者不同事务，一次失败的动作会永久占着幂等键，重试直接被判为
 * "已受理"返回空结果，单据卡死在原状态。
 *
 * <p>并发下的最终保证是 {@code uk_wms_command_idempotency_deleted} 唯一索引，
 * 前置查询只是为了在常见场景下少抛一次异常。
 */
@Service
public class WmsCommandServiceImpl implements WmsCommandService {

    @Resource
    private WmsCommandRecordMapper commandRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long execute(WmsCommandTypeEnum commandType, WmsCommandReqDTO command, Supplier<Long> action) {
        WmsCommandRecordDO accepted = commandRecordMapper.selectByIdempotencyKey(command.getIdempotencyKey());
        if (accepted != null) {
            return accepted.getResultId();
        }
        Long resultId = action.get();
        commandRecordMapper.insert(WmsCommandRecordDO.builder()
                .idempotencyKey(command.getIdempotencyKey())
                .commandType(commandType.name())
                .sourceModule(command.getSourceModule())
                .sourceBizNo(command.getSourceBizNo())
                .sourceBizLineNo(command.getSourceBizLineNo())
                .resultId(resultId)
                .build());
        return resultId;
    }

}
