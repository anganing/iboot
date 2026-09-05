package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqActionReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 领退补料申请 API 实现。 */
@Service
@Validated
public class MaterialReqApiImpl implements MaterialReqApi {

    @Override
    public Long createMaterialReq(CreateMaterialReqReqDTO reqDTO) {
        throw new UnsupportedOperationException("领退补料申请创建命令尚未接入领域规则");
    }

    @Override
    public void cancelMaterialReq(MaterialReqActionReqDTO reqDTO) {
        throw new UnsupportedOperationException("领退补料申请取消命令尚未接入领域规则");
    }

    @Override
    public void confirmMaterialReq(MaterialReqActionReqDTO reqDTO) {
        throw new UnsupportedOperationException("领退补料申请确认命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public MaterialReqRespDTO getMaterialReq(Long id) {
        throw new UnsupportedOperationException("领退补料申请查询尚未接入领域 Service");
    }

}
