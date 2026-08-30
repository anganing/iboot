package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.CreateReq;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;

/** 领料、退料与补料申请 API 门面。确认后由 Service 调用 WMS 单据 API。 */
public interface MaterialReqApi {
    Long createMaterialReq(CreateReq reqDTO);
    void cancelMaterialReq(MesCommandReqDTO reqDTO);
    void confirmMaterialReq(MesCommandReqDTO reqDTO);
    MesRespDTO getMaterialReq(Long id);
}
