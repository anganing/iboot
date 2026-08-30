package com.iboot.pf.module.aps.api.load;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.load.dto.*;
import com.iboot.pf.module.aps.service.load.ApsLoadService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * APS 负荷 API 实现。
 */
@Service
@Validated
public class LoadApiImpl implements LoadApi {
    @Resource
    private ApsLoadService service;

    @DataPermission(enable = false)
    public List<LoadRespDTO> getLoadListByVersionId(Long id) {
        return BeanUtils.toBean(service.getLoadListByVersionId(id), LoadRespDTO.class);
    }

    public void refreshLoad(RefreshLoadReqDTO r) {
        service.refreshLoad(r);
    }
}
