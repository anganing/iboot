package com.iboot.pf.module.aps.api.load;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.load.dto.LoadRespDTO;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import com.iboot.pf.module.aps.service.load.ApsLoadService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** APS 资源负荷 API 实现。 */
@Service
@Validated
public class LoadApiImpl implements LoadApi {

    @Resource
    private ApsLoadService loadService;

    @Override
    @DataPermission(enable = false)
    public List<LoadRespDTO> getLoadListByVersionId(Long versionId) {
        return BeanUtils.toBean(loadService.getLoadDOListByVersionId(versionId), LoadRespDTO.class);
    }

    @Override
    public void refreshLoad(RefreshLoadReqDTO reqDTO) {
        loadService.refreshLoad(reqDTO);
    }

}
