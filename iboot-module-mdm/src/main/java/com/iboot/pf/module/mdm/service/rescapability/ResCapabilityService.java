package com.iboot.pf.module.mdm.service.rescapability;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityRespVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilitySaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.rescapability.ResCapabilityDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 资源能力 Service 接口
 *
 * @author iboot
 */
public interface ResCapabilityService {

    /**
     * 创建资源能力
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createResCapability(@Valid ResCapabilitySaveReqVO createReqVO);

    /**
     * 更新资源能力
     *
     * @param updateReqVO 更新信息
     */
    void updateResCapability(@Valid ResCapabilitySaveReqVO updateReqVO);

    /**
     * 删除资源能力
     *
     * @param id 编号
     */
    void deleteResCapability(Long id);

    /**
     * 批量删除资源能力
     *
     * @param ids 编号列表
     */
    void deleteResCapabilityListByIds(List<Long> ids);

    /**
     * 获得资源能力
     *
     * @param id 编号
     * @return 资源能力
     */
    ResCapabilityRespVO getResCapability(Long id);

    /**
     * 获得资源能力分页
     *
     * @param pageReqVO 分页查询
     * @return 资源能力分页
     */
    PageResult<ResCapabilityRespVO> getResCapabilityPage(ResCapabilityPageReqVO pageReqVO);

    /**
     * 获得工序的能力候选资源列表
     *
     * @param opId 标准工序编号
     * @return 资源能力列表
     */
    List<ResCapabilityDO> getResCapabilityListByOpId(Long opId);

    /**
     * 按标准工序编号集合批量查询启用中的资源能力
     *
     * <p>仅返回启用记录，按 opId + preferredFlag desc + resId 排序。
     *
     * @param opIds 标准工序编号集合
     * @return 资源能力列表；空集合返回空列表
     */
    List<ResCapabilityDO> getResCapabilityList(Collection<Long> opIds);

}
