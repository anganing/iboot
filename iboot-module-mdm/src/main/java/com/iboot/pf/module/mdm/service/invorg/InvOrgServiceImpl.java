package com.iboot.pf.module.mdm.service.invorg;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.invorg.InvOrgDO;
import com.iboot.pf.module.mdm.dal.mysql.invorg.InvOrgMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.INV_ORG_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.INV_ORG;

@Service
@Validated
public class InvOrgServiceImpl implements InvOrgService {

    @Resource
    private InvOrgMapper invOrgMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    @Override
    public Long createInvOrg(InvOrgSaveReqVO createReqVO) {
        InvOrgDO invOrg = BeanUtils.toBean(createReqVO, InvOrgDO.class);
        invOrg.setId(null);
        invOrg.setCode(mdmCodeService.resolveCode(invOrg.getCode(), INV_ORG));
        invOrgMapper.insert(invOrg);
        return invOrg.getId();
    }

    @Override
    public void updateInvOrg(InvOrgSaveReqVO updateReqVO) {
        validateInvOrgExists(updateReqVO.getId());
        invOrgMapper.updateById(BeanUtils.toBean(updateReqVO, InvOrgDO.class));
    }

    @Override
    public void deleteInvOrg(Long id) {
        validateInvOrgExists(id);
        invOrgMapper.deleteById(id);
    }

    @Override
    public InvOrgDO getInvOrg(Long id) {
        return invOrgMapper.selectById(id);
    }

    @Override
    public PageResult<InvOrgDO> getInvOrgPage(InvOrgPageReqVO pageReqVO) {
        return invOrgMapper.selectPage(pageReqVO);
    }

    private void validateInvOrgExists(Long id) {
        if (invOrgMapper.selectById(id) == null) {
            throw exception(INV_ORG_NOT_EXISTS);
        }
    }

}
