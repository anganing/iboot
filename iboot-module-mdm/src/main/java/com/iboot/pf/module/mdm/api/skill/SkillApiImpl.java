package com.iboot.pf.module.mdm.api.skill;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.skill.dto.MemberSkillRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.SkillRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.MemberSkillDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.OpSkillReqDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.SkillDO;
import com.iboot.pf.module.mdm.service.skill.SkillService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 技能 API 实现类
 *
 * @author iboot
 */
@Service
public class SkillApiImpl implements SkillApi {

    @Resource
    private SkillService skillService;

    @Override
    @DataPermission(enable = false)
    public List<SkillRespDTO> getSkillList() {
        List<SkillDO> list = skillService.getSkillList();
        return BeanUtils.toBean(list, SkillRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<OpSkillReqRespDTO> getOpSkillReqList(Collection<Long> opIds) {
        List<OpSkillReqDO> list = skillService.getOpSkillReqList(opIds);
        return BeanUtils.toBean(list, OpSkillReqRespDTO.class);
    }

    @Override
    @DataPermission(enable = false)
    public List<MemberSkillRespDTO> getMemberSkillList(Collection<Long> userIds) {
        List<MemberSkillDO> list = skillService.getMemberSkillList(userIds);
        return BeanUtils.toBean(list, MemberSkillRespDTO.class);
    }

}
