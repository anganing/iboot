package com.iboot.pf.module.mdm.service.skill;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.skill.MemberSkillDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.OpSkillReqDO;
import com.iboot.pf.module.mdm.dal.dataobject.skill.SkillDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 技能 Service 接口
 *
 * @author iboot
 */
public interface SkillService {

    // ==================== 技能 ====================

    /**
     * 创建技能
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSkill(@Valid SkillSaveReqVO createReqVO);

    /**
     * 更新技能
     *
     * @param updateReqVO 更新信息
     */
    void updateSkill(@Valid SkillSaveReqVO updateReqVO);

    /**
     * 删除技能
     *
     * @param id 编号
     */
    void deleteSkill(Long id);

    /**
     * 批量删除技能
     *
     * @param ids 编号列表
     */
    void deleteSkillListByIds(List<Long> ids);

    /**
     * 获得技能
     *
     * @param id 编号
     * @return 技能
     */
    SkillRespVO getSkill(Long id);

    /**
     * 获得技能分页
     *
     * @param pageReqVO 分页查询
     * @return 技能分页
     */
    PageResult<SkillRespVO> getSkillPage(SkillPageReqVO pageReqVO);

    /**
     * 获得全部技能（含停用，供门面批量查询）
     *
     * @return 技能列表
     */
    List<SkillDO> getSkillList();

    // ==================== 工序技能要求 ====================

    /**
     * 创建工序技能要求
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpSkillReq(@Valid OpSkillReqSaveReqVO createReqVO);

    /**
     * 更新工序技能要求
     *
     * @param updateReqVO 更新信息
     */
    void updateOpSkillReq(@Valid OpSkillReqSaveReqVO updateReqVO);

    /**
     * 删除工序技能要求
     *
     * @param id 编号
     */
    void deleteOpSkillReq(Long id);

    /**
     * 批量删除工序技能要求
     *
     * @param ids 编号列表
     */
    void deleteOpSkillReqListByIds(List<Long> ids);

    /**
     * 获得工序技能要求
     *
     * @param id 编号
     * @return 工序技能要求
     */
    OpSkillReqRespVO getOpSkillReq(Long id);

    /**
     * 获得工序技能要求分页
     *
     * @param pageParam 分页参数
     * @param opId 标准工序编号
     * @return 工序技能要求分页
     */
    PageResult<OpSkillReqRespVO> getOpSkillReqPage(PageParam pageParam, Long opId);

    /**
     * 获得工序的技能要求列表
     *
     * @param opId 标准工序编号
     * @return 技能要求列表
     */
    List<OpSkillReqDO> getOpSkillReqListByOpId(Long opId);

    /**
     * 按标准工序编号集合批量查询技能要求（供门面批量查询）
     *
     * @param opIds 标准工序编号集合
     * @return 技能要求列表；空集合返回空列表
     */
    List<OpSkillReqDO> getOpSkillReqList(Collection<Long> opIds);

    // ==================== 人员技能 ====================

    /**
     * 创建人员技能
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMemberSkill(@Valid MemberSkillSaveReqVO createReqVO);

    /**
     * 更新人员技能
     *
     * @param updateReqVO 更新信息
     */
    void updateMemberSkill(@Valid MemberSkillSaveReqVO updateReqVO);

    /**
     * 删除人员技能
     *
     * @param id 编号
     */
    void deleteMemberSkill(Long id);

    /**
     * 批量删除人员技能
     *
     * @param ids 编号列表
     */
    void deleteMemberSkillListByIds(List<Long> ids);

    /**
     * 获得人员技能
     *
     * @param id 编号
     * @return 人员技能
     */
    MemberSkillRespVO getMemberSkill(Long id);

    /**
     * 获得人员技能分页
     *
     * @param pageParam 分页参数
     * @param userId 用户编号
     * @param skillId 技能编号
     * @return 人员技能分页
     */
    PageResult<MemberSkillRespVO> getMemberSkillPage(PageParam pageParam, Long userId, Long skillId);

    /**
     * 获得人员的有效技能列表（校验资质时使用）
     *
     * @param userId 用户编号
     * @param now 当前日期，用于剔除过期技能
     * @return 有效技能列表
     */
    List<MemberSkillDO> getMemberSkillListByUserId(Long userId, java.time.LocalDate now);

    /**
     * 按用户编号集合批量查询人员技能（含停用，供门面批量查询）
     *
     * @param userIds system 用户编号集合
     * @return 人员技能列表；空集合返回空列表
     */
    List<MemberSkillDO> getMemberSkillList(Collection<Long> userIds);

}
