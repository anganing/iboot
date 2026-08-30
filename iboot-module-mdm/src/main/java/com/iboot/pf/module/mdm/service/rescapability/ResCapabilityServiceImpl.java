package com.iboot.pf.module.mdm.service.rescapability;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilityRespVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilitySaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescapability.ResCapabilityDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescapability.ResCapabilityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.OP_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CAPABILITY_DUPLICATE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CAPABILITY_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_NOT_EXISTS;

/**
 * 资源能力 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ResCapabilityServiceImpl implements ResCapabilityService {

    @Resource
    private ResCapabilityMapper resCapabilityMapper;
    @Resource
    private ResMapper resMapper;
    @Resource
    private OpMapper opMapper;

    @Override
    public Long createResCapability(ResCapabilitySaveReqVO createReqVO) {
        validateResExists(createReqVO.getResId());
        validateOpExists(createReqVO.getOpId());
        validateUnique(createReqVO.getResId(), createReqVO.getOpId(), null);
        ResCapabilityDO capability = BeanUtils.toBean(createReqVO, ResCapabilityDO.class);
        capability.setId(null);
        resCapabilityMapper.insert(capability);
        return capability.getId();
    }

    @Override
    public void updateResCapability(ResCapabilitySaveReqVO updateReqVO) {
        validateResCapabilityExists(updateReqVO.getId());
        validateResExists(updateReqVO.getResId());
        validateOpExists(updateReqVO.getOpId());
        validateUnique(updateReqVO.getResId(), updateReqVO.getOpId(), updateReqVO.getId());
        ResCapabilityDO updateObj = BeanUtils.toBean(updateReqVO, ResCapabilityDO.class);
        resCapabilityMapper.updateById(updateObj);
    }

    @Override
    public void deleteResCapability(Long id) {
        validateResCapabilityExists(id);
        resCapabilityMapper.deleteById(id);
    }

    @Override
    public void deleteResCapabilityListByIds(List<Long> ids) {
        resCapabilityMapper.deleteByIds(ids);
    }

    private void validateResCapabilityExists(Long id) {
        if (resCapabilityMapper.selectById(id) == null) {
            throw exception(RES_CAPABILITY_NOT_EXISTS);
        }
    }

    private void validateResExists(Long resId) {
        ResDO res = resMapper.selectById(resId);
        if (res == null) {
            throw exception(RES_NOT_EXISTS);
        }
    }

    private void validateOpExists(Long opId) {
        OpDO op = opMapper.selectById(opId);
        if (op == null) {
            throw exception(OP_NOT_EXISTS);
        }
    }

    private void validateUnique(Long resId, Long opId, Long excludeId) {
        ResCapabilityDO existing = resCapabilityMapper.selectByResIdAndOpId(resId, opId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(RES_CAPABILITY_DUPLICATE);
        }
    }

    @Override
    public ResCapabilityRespVO getResCapability(Long id) {
        return resCapabilityMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ResCapabilityRespVO> getResCapabilityPage(ResCapabilityPageReqVO pageReqVO) {
        return resCapabilityMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public List<ResCapabilityDO> getResCapabilityListByOpId(Long opId) {
        return resCapabilityMapper.selectListByOpId(opId);
    }

    @Override
    public List<ResCapabilityDO> getResCapabilityList(Collection<Long> opIds) {
        if (opIds == null || opIds.isEmpty()) {
            return List.of();
        }
        List<ResCapabilityDO> result = new ArrayList<>();
        for (Long opId : opIds) {
            for (ResCapabilityDO capability : resCapabilityMapper.selectListByOpId(opId)) {
                // 仅返回启用记录
                if (CommonStatusEnum.isEnable(capability.getStatus())) {
                    result.add(capability);
                }
            }
        }
        // 按 opId + preferredFlag desc + resId 排序
        result.sort(Comparator
                .comparing(ResCapabilityDO::getOpId)
                .thenComparing(ResCapabilityDO::getPreferredFlag, Comparator.reverseOrder())
                .thenComparing(ResCapabilityDO::getResId));
        return result;
    }

}
