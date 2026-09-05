package com.iboot.pf.module.mes.api.rework;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.rework.dto.CreateReworkReqDTO;
import com.iboot.pf.module.mes.api.rework.dto.ReworkRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 返工任务 API 实现。 */
@Service
@Validated
public class ReworkApiImpl implements ReworkApi {

    @Override
    public Long createRework(CreateReworkReqDTO reqDTO) {
        throw new UnsupportedOperationException("返工任务创建命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public ReworkRespDTO getRework(Long id) {
        throw new UnsupportedOperationException("返工任务查询尚未接入领域 Service");
    }

    @Override
    @DataPermission(enable = false)
    public List<ReworkRespDTO> getReworkList(Collection<Long> ids) {
        throw new UnsupportedOperationException("返工任务查询尚未接入领域 Service");
    }

}
