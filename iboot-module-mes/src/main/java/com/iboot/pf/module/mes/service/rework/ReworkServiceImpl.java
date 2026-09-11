package com.iboot.pf.module.mes.service.rework;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_STATUS_INVALID;

/**
 * MES-返工任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ReworkServiceImpl implements ReworkService {

    @Resource
    private ReworkMapper reworkMapper;
    @Resource
    private MesReworkDomainService reworkDomainService;

    @Override
    public Long createRework(ReworkSaveReqVO createReqVO) {
        ReworkDO rework = BeanUtils.toBean(createReqVO, ReworkDO.class);
        rework.setId(null);
        return reworkDomainService.createRework(rework);
    }

    @Override
    public void updateRework(ReworkSaveReqVO updateReqVO) {
        ReworkDO existed = validateReworkExists(updateReqVO.getId());
        // 只有新建状态允许改，已下达的返工任务已经在车间执行
        if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
            throw exception(REWORK_STATUS_INVALID);
        }
        ReworkDO updateObj = BeanUtils.toBean(updateReqVO, ReworkDO.class);
        updateObj.setReworkNo(existed.getReworkNo());
        // 返工单的来源工序、单据链和路线版本在领域创建时锁定，草稿修改只允许维护描述字段与数量。
        updateObj.setSourceQcTaskId(existed.getSourceQcTaskId());
        updateObj.setProdOrderId(existed.getProdOrderId());
        updateObj.setWorkOrderId(existed.getWorkOrderId());
        updateObj.setOpTaskId(existed.getOpTaskId());
        updateObj.setRouteId(existed.getRouteId());
        updateObj.setRouteVersion(existed.getRouteVersion());
        updateObj.setStatus(existed.getStatus());
        reworkMapper.updateById(updateObj);
    }

    @Override
    public void deleteRework(Long id) {
        ReworkDO existed = validateReworkExists(id);
        if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
            throw exception(REWORK_STATUS_INVALID);
        }
        reworkMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReworkListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            ReworkDO existed = validateReworkExists(id);
            if (!MesReworkStatusEnum.isDraft(existed.getStatus())) {
                throw exception(REWORK_STATUS_INVALID);
            }
        }
        reworkMapper.deleteByIds(ids);
    }

    private ReworkDO validateReworkExists(Long id) {
        ReworkDO rework = id == null ? null : reworkMapper.selectById(id);
        if (rework == null) {
            throw exception(REWORK_NOT_EXISTS);
        }
        return rework;
    }

    @Override
    public ReworkRespVO getRework(Long id) {
        return reworkMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ReworkRespVO> getReworkPage(ReworkPageReqVO pageReqVO) {
        return reworkMapper.selectPage(pageReqVO);
    }

}
