package com.iboot.pf.module.mdm.service.invorg;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import jakarta.validation.Valid;

public interface InvOrgService {

    Long createInvOrg(@Valid InvOrgSaveReqVO createReqVO);

    void updateInvOrg(@Valid InvOrgSaveReqVO updateReqVO);

    void deleteInvOrg(Long id);

    InvOrgDO getInvOrg(Long id);

    PageResult<InvOrgDO> getInvOrgPage(InvOrgPageReqVO pageReqVO);

    /**
     * 获得全部库存组织（含停用）
     *
     * @return 库存组织列表
     */
    java.util.List<InvOrgDO> getInvOrgList();
}
