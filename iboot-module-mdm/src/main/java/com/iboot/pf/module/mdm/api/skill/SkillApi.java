package com.iboot.pf.module.mdm.api.skill;

import com.iboot.pf.module.mdm.api.skill.dto.MemberSkillRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.OpSkillReqRespDTO;
import com.iboot.pf.module.mdm.api.skill.dto.SkillRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 技能 API 接口
 *
 * <p>对 MES 提供技能、工序技能要求、人员技能门面。
 *
 * @author iboot
 */
public interface SkillApi {

    /**
     * 获得全部技能
     *
     * @return 技能列表；无数据返回空列表
     */
    List<SkillRespDTO> getSkillList();

    /**
     * 按标准工序编号集合批量查询技能要求
     *
     * @param opIds 标准工序编号集合
     * @return 技能要求列表；空集合返回空列表
     */
    List<OpSkillReqRespDTO> getOpSkillReqList(Collection<Long> opIds);

    /**
     * 按用户编号集合批量查询人员技能
     *
     * @param userIds system 用户编号集合
     * @return 人员技能列表；空集合返回空列表
     */
    List<MemberSkillRespDTO> getMemberSkillList(Collection<Long> userIds);

}
