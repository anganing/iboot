package com.iboot.pf.module.aps.service.command;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.command.ApsCommandRecordDO;
import com.iboot.pf.module.aps.dal.mysql.command.ApsCommandRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Supplier;

/** APS 跨模块命令幂等服务实现。 */
@Service
public class ApsCommandServiceImpl implements ApsCommandService {
    @Resource private ApsCommandRecordMapper commandRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long execute(String commandType, ApsCommandReqDTO command, Supplier<Long> action) {
        ApsCommandRecordDO accepted = commandRecordMapper.selectOne(ApsCommandRecordDO::getIdempotencyKey,
                command.getIdempotencyKey());
        if (accepted != null) {
            return accepted.getResultId();
        }
        Long resultId = action.get();
        commandRecordMapper.insert(ApsCommandRecordDO.builder()
                .idempotencyKey(command.getIdempotencyKey()).commandType(commandType)
                .sourceModule(command.getSourceModule()).sourceBizNo(command.getSourceBizNo())
                .sourceBizLineNo(command.getSourceBizLineNo()).resultId(resultId).build());
        return resultId;
    }
}
