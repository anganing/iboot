package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqActionReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqRespDTO;

/**
 * 领退补料申请 API 门面。
 *
 * <p>三段口径：创建落草稿、提交占预留、确认走 WMS 出入库过账。确认后由领域 Service 调用 WMS 单据 API。
 */
public interface MaterialReqApi {

    /**
     * 创建领退补料申请，落 {@code DRAFT}。
     *
     * @param reqDTO 创建命令
     * @return 领退补料申请编号
     */
    Long createMaterialReq(CreateMaterialReqReqDTO reqDTO);

    /**
     * 取消领退补料申请，已提交的先释放预留。
     *
     * @param reqDTO 动作命令
     */
    void cancelMaterialReq(MaterialReqActionReqDTO reqDTO);

    /**
     * 确认领退补料申请，调 WMS 建单并过账。
     *
     * @param reqDTO 动作命令
     */
    void confirmMaterialReq(MaterialReqActionReqDTO reqDTO);

    /**
     * 查询单张领退补料申请，含明细行。
     *
     * @param id 领退补料申请编号
     * @return 申请事实，不存在时返回 null
     */
    MaterialReqRespDTO getMaterialReq(Long id);

}
