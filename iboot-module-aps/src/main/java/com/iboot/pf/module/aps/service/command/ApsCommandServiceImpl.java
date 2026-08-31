package com.iboot.pf.module.aps.service.command;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.command.ApsCommandRecordDO;
import com.iboot.pf.module.aps.dal.mysql.command.ApsCommandRecordMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * APS 跨模块命令幂等服务实现。
 *
 * <p>幂等记录与业务动作在同一个事务里落库：动作失败时记录一起回滚，重试才能重新执行。
 * 并发下的最终保证是 {@code uk_aps_command_idempotency_deleted} 唯一索引，这里的前置查询
 * 只是为了在常见场景下少抛一次异常。
 */
@Service
public class ApsCommandServiceImpl implements ApsCommandService {

    @Resource
    private ApsCommandRecordMapper commandRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long execute(ApsCommandTypeEnum commandType, ApsCommandReqDTO command, Supplier<Long> action) {
        ApsCommandRecordDO accepted = commandRecordMapper.selectByIdempotencyKey(command.getIdempotencyKey());
        if (accepted != null) {
            return accepted.getResultId();
        }
        Long resultId = action.get();
        commandRecordMapper.insert(ApsCommandRecordDO.builder()
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
