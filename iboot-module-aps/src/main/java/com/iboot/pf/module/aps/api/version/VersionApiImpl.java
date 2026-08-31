package com.iboot.pf.module.aps.api.version;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.version.dto.ChangeVersionStatusReqDTO;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.api.version.dto.ReplaceVersionReqDTO;
import com.iboot.pf.module.aps.api.version.dto.VersionRespDTO;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** 排程版本 API 实现。 */
@Service
@Validated
public class VersionApiImpl implements VersionApi {

    @Resource
    private ApsVersionService versionService;

    @Override
    public Long generateVersion(GenerateVersionReqDTO reqDTO) {
        return versionService.generateVersion(reqDTO);
    }

    @Override
    @DataPermission(enable = false)
    public VersionRespDTO getVersion(Long id) {
        return BeanUtils.toBean(versionService.getVersionDO(id), VersionRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<VersionRespDTO> getVersionListByScenarioId(Long scenarioId) {
        return BeanUtils.toBean(versionService.getVersionDOListByScenarioId(scenarioId), VersionRespDTO.class);
    }

    @Override
    public void confirmVersion(ChangeVersionStatusReqDTO reqDTO) {
        versionService.confirmVersion(reqDTO);
    }

    @Override
    public void publishVersion(ChangeVersionStatusReqDTO reqDTO) {
        versionService.publishVersion(reqDTO);
    }

    @Override
    public void replaceVersion(ReplaceVersionReqDTO reqDTO) {
        versionService.replaceVersion(reqDTO);
    }

}
