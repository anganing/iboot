package com.iboot.pf.module.mdm.service.skill;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.MemberSkillDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.OpSkillReqDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.SkillDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.skill.MemberSkillMapper;
import com.iboot.pf.module.mdm.dal.mysql.skill.OpSkillReqMapper;
import com.iboot.pf.module.mdm.dal.mysql.skill.SkillMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.SKILL;

/**
 * 技能 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class SkillServiceImpl implements SkillService {

    @Resource
    private SkillMapper skillMapper;
    @Resource
    private OpSkillReqMapper opSkillReqMapper;
    @Resource
    private MemberSkillMapper memberSkillMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private MdmCodeService mdmCodeService;

    // ==================== 技能 ====================

    @Override
    public Long createSkill(SkillSaveReqVO createReqVO) {
        SkillDO skill = BeanUtils.toBean(createReqVO, SkillDO.class);
        skill.setId(null);
        skill.setCode(mdmCodeService.resolveCode(skill.getCode(), SKILL));
        skillMapper.insert(skill);
        return skill.getId();
    }

    @Override
    public void updateSkill(SkillSaveReqVO updateReqVO) {
        validateSkillExists(updateReqVO.getId());
        SkillDO updateObj = BeanUtils.toBean(updateReqVO, SkillDO.class);
        skillMapper.updateById(updateObj);
    }

    @Override
    public void deleteSkill(Long id) {
        validateSkillExists(id);
        // 被工序技能要求引用的技能不允许删除
        if (!opSkillReqMapper.selectList(OpSkillReqDO::getSkillId, id).isEmpty()) {
            throw exception(SKILL_DELETE_REFERENCED);
        }
        skillMapper.deleteById(id);
    }

    @Override
    public void deleteSkillListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteSkill(id);
        }
    }

    private SkillDO validateSkillExists(Long id) {
        SkillDO skill = skillMapper.selectById(id);
        if (skill == null) {
            throw exception(SKILL_NOT_EXISTS);
        }
        return skill;
    }

    @Override
    public SkillRespVO getSkill(Long id) {
        return BeanUtils.toBean(skillMapper.selectById(id), SkillRespVO.class);
    }

    @Override
    public PageResult<SkillRespVO> getSkillPage(SkillPageReqVO pageReqVO) {
        PageResult<SkillDO> pageResult = skillMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, SkillRespVO.class);
    }

    // ==================== 工序技能要求 ====================

    @Override
    public Long createOpSkillReq(OpSkillReqSaveReqVO createReqVO) {
        validateOpExists(createReqVO.getOpId());
        validateSkillExists(createReqVO.getSkillId());
        validateOpSkillReqUnique(createReqVO.getOpId(), createReqVO.getSkillId(), null);
        OpSkillReqDO reqDO = BeanUtils.toBean(createReqVO, OpSkillReqDO.class);
        reqDO.setId(null);
        opSkillReqMapper.insert(reqDO);
        return reqDO.getId();
    }

    @Override
    public void updateOpSkillReq(OpSkillReqSaveReqVO updateReqVO) {
        if (opSkillReqMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(OP_SKILL_REQ_NOT_EXISTS);
        }
        validateOpExists(updateReqVO.getOpId());
        validateSkillExists(updateReqVO.getSkillId());
        validateOpSkillReqUnique(updateReqVO.getOpId(), updateReqVO.getSkillId(), updateReqVO.getId());
        OpSkillReqDO updateObj = BeanUtils.toBean(updateReqVO, OpSkillReqDO.class);
        opSkillReqMapper.updateById(updateObj);
    }

    @Override
    public void deleteOpSkillReq(Long id) {
        if (opSkillReqMapper.selectById(id) == null) {
            throw exception(OP_SKILL_REQ_NOT_EXISTS);
        }
        opSkillReqMapper.deleteById(id);
    }

    private void validateOpSkillReqUnique(Long opId, Long skillId, Long excludeId) {
        OpSkillReqDO existing = opSkillReqMapper.selectByOpIdAndSkillId(opId, skillId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(OP_SKILL_REQ_DUPLICATE);
        }
    }

    private void validateOpExists(Long opId) {
        OpDO op = opMapper.selectById(opId);
        if (op == null) {
            throw exception(OP_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<OpSkillReqRespVO> getOpSkillReqPage(PageParam pageParam, Long opId) {
        return opSkillReqMapper.selectJoinPage(pageParam, opId);
    }

    @Override
    public List<OpSkillReqDO> getOpSkillReqListByOpId(Long opId) {
        return opSkillReqMapper.selectListByOpId(opId);
    }

    // ==================== 人员技能 ====================

    @Override
    public Long createMemberSkill(MemberSkillSaveReqVO createReqVO) {
        adminUserApi.validateUser(createReqVO.getUserId());
        validateSkillExists(createReqVO.getSkillId());
        validateMemberSkillDate(createReqVO.getEffectiveDate(), createReqVO.getExpiryDate());
        validateMemberSkillUnique(createReqVO.getUserId(), createReqVO.getSkillId(), null);
        MemberSkillDO memberSkill = BeanUtils.toBean(createReqVO, MemberSkillDO.class);
        memberSkill.setId(null);
        memberSkillMapper.insert(memberSkill);
        return memberSkill.getId();
    }

    @Override
    public void updateMemberSkill(MemberSkillSaveReqVO updateReqVO) {
        if (memberSkillMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(MEMBER_SKILL_NOT_EXISTS);
        }
        adminUserApi.validateUser(updateReqVO.getUserId());
        validateSkillExists(updateReqVO.getSkillId());
        validateMemberSkillDate(updateReqVO.getEffectiveDate(), updateReqVO.getExpiryDate());
        validateMemberSkillUnique(updateReqVO.getUserId(), updateReqVO.getSkillId(), updateReqVO.getId());
        MemberSkillDO updateObj = BeanUtils.toBean(updateReqVO, MemberSkillDO.class);
        memberSkillMapper.updateById(updateObj);
    }

    @Override
    public void deleteMemberSkill(Long id) {
        if (memberSkillMapper.selectById(id) == null) {
            throw exception(MEMBER_SKILL_NOT_EXISTS);
        }
        memberSkillMapper.deleteById(id);
    }

    private void validateMemberSkillDate(LocalDate effectiveDate, LocalDate expiryDate) {
        if (effectiveDate != null && expiryDate != null && expiryDate.isBefore(effectiveDate)) {
            throw exception(MEMBER_SKILL_DATE_INVALID);
        }
    }

    private void validateMemberSkillUnique(Long userId, Long skillId, Long excludeId) {
        MemberSkillDO existing = memberSkillMapper.selectByUserIdAndSkillId(userId, skillId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(MEMBER_SKILL_DUPLICATE);
        }
    }

    @Override
    public PageResult<MemberSkillRespVO> getMemberSkillPage(PageParam pageParam, Long userId, Long skillId) {
        return memberSkillMapper.selectJoinPage(pageParam, userId, skillId);
    }

    @Override
    public List<MemberSkillDO> getMemberSkillListByUserId(Long userId, LocalDate now) {
        return memberSkillMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MemberSkillDO>()
                .eq(MemberSkillDO::getUserId, userId)
                .eq(MemberSkillDO::getStatus, 0)
                .le(MemberSkillDO::getEffectiveDate, now)
                .and(wrapper -> wrapper.isNull(MemberSkillDO::getExpiryDate)
                        .or()
                        .ge(MemberSkillDO::getExpiryDate, now)));
    }

}
