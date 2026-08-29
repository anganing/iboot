package com.iboot.pf.module.mdm.service.changeover;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverRespVO;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.changeover.ResChangeoverDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.mysql.changeover.ResChangeoverMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CHANGEOVER_DUPLICATE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CHANGEOVER_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CHANGEOVER_SAME_GROUP;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_NOT_EXISTS;

/**
 * 资源换型矩阵 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ResChangeoverServiceImpl implements ResChangeoverService {

    @Resource
    private ResChangeoverMapper resChangeoverMapper;
    @Resource
    private ResMapper resMapper;

    @Override
    public Long createResChangeover(ResChangeoverSaveReqVO createReqVO) {
        validateFields(createReqVO);
        validateUnique(createReqVO.getResId(), createReqVO.getFromGroup(), createReqVO.getToGroup(), null);
        ResChangeoverDO changeover = BeanUtils.toBean(createReqVO, ResChangeoverDO.class);
        changeover.setId(null);
        resChangeoverMapper.insert(changeover);
        return changeover.getId();
    }

    @Override
    public void updateResChangeover(ResChangeoverSaveReqVO updateReqVO) {
        if (resChangeoverMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(RES_CHANGEOVER_NOT_EXISTS);
        }
        validateFields(updateReqVO);
        validateUnique(updateReqVO.getResId(), updateReqVO.getFromGroup(), updateReqVO.getToGroup(), updateReqVO.getId());
        ResChangeoverDO updateObj = BeanUtils.toBean(updateReqVO, ResChangeoverDO.class);
        resChangeoverMapper.updateById(updateObj);
    }

    private void validateFields(ResChangeoverSaveReqVO reqVO) {
        // 按资源生效时校验资源存在；按资源组生效时必须填写资源组
        if (reqVO.getResId() != null && reqVO.getResId() != 0L) {
            ResDO res = resMapper.selectById(reqVO.getResId());
            if (res == null) {
                throw exception(RES_NOT_EXISTS);
            }
        } else if (reqVO.getResGroup() == null || reqVO.getResGroup().isBlank()) {
            throw exception(RES_CHANGEOVER_NOT_EXISTS);
        }
        // 同组换型无需配置
        if (reqVO.getFromGroup().equals(reqVO.getToGroup())) {
            throw exception(RES_CHANGEOVER_SAME_GROUP);
        }
    }

    private void validateUnique(Long resId, String fromGroup, String toGroup, Long excludeId) {
        ResChangeoverDO existing = resChangeoverMapper.selectByResIdAndGroups(resId, fromGroup, toGroup);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(RES_CHANGEOVER_DUPLICATE);
        }
    }

    @Override
    public void deleteResChangeover(Long id) {
        if (resChangeoverMapper.selectById(id) == null) {
            throw exception(RES_CHANGEOVER_NOT_EXISTS);
        }
        resChangeoverMapper.deleteById(id);
    }

    @Override
    public void deleteResChangeoverListByIds(List<Long> ids) {
        resChangeoverMapper.deleteByIds(ids);
    }

    @Override
    public ResChangeoverRespVO getResChangeover(Long id) {
        return resChangeoverMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ResChangeoverRespVO> getResChangeoverPage(ResChangeoverPageReqVO pageReqVO) {
        return resChangeoverMapper.selectJoinPage(pageReqVO);
    }

}
