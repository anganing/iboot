package com.iboot.pf.module.aps.api.shortage;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.api.shortage.dto.ShortageRespDTO;
import com.iboot.pf.module.aps.service.shortage.ApsShortageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** APS 缺料 API 实现。 */
@Service
@Validated
public class ShortageApiImpl implements ShortageApi {

    @Resource
    private ApsShortageService shortageService;

    @Override
    @DataPermission(enable = false)
    public List<ShortageRespDTO> getShortageListByVersionId(Long versionId) {
        return BeanUtils.toBean(shortageService.getShortageDOListByVersionId(versionId), ShortageRespDTO.class);
    }

    @Override
    public void refreshShortage(RefreshShortageReqDTO reqDTO) {
        shortageService.refreshShortage(reqDTO);
    }

}
