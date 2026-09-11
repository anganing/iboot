package com.iboot.pf.module.mes.api.rework;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.rework.dto.CreateReworkReqDTO;
import com.iboot.pf.module.mes.api.rework.dto.ReworkRespDTO;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.service.rework.MesReworkDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 返工任务 API 实现。 */
@Service
@Validated
@DataPermission(enable = false)
public class ReworkApiImpl implements ReworkApi {

    @Resource
    private MesReworkDomainService reworkDomainService;
    @Resource
    private ReworkMapper reworkMapper;

    @Override
    public Long createRework(CreateReworkReqDTO reqDTO) {
        ReworkDO rework = BeanUtils.toBean(reqDTO, ReworkDO.class);
        return reworkDomainService.createRework(rework);
    }

    @Override
    public ReworkRespDTO getRework(Long id) {
        return BeanUtils.toBean(id == null ? null : reworkMapper.selectById(id), ReworkRespDTO.class);
    }

    @Override
    public List<ReworkRespDTO> getReworkList(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of()
                : BeanUtils.toBean(reworkMapper.selectByIds(ids), ReworkRespDTO.class);
    }

}
