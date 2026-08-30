package com.iboot.pf.module.mdm.service.res;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.res.vo.ResPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.res.vo.ResRespVO;
import com.iboot.pf.module.mdm.controller.admin.res.vo.ResSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescapability.ResCapabilityMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_DELETE_REFERENCED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.WC_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.RES;

/**
 * 生产资源 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ResServiceImpl implements ResService {

    @Resource
    private ResMapper resMapper;
    @Resource
    private ResCapabilityMapper resCapabilityMapper;
    @Resource
    private MdmCodeService mdmCodeService;
    @Resource
    private WcMapper wcMapper;

    @Override
    public Long createRes(ResSaveReqVO createReqVO) {
        validateWcExists(createReqVO.getWcId());
        // 插入
        ResDO res = BeanUtils.toBean(createReqVO, ResDO.class);
        res.setId(null);
        res.setCode(mdmCodeService.resolveCode(res.getCode(), RES));
        resMapper.insert(res);

        // 返回
        return res.getId();
    }

    @Override
    public void updateRes(ResSaveReqVO updateReqVO) {
        // 校验存在
        validateResExists(updateReqVO.getId());
        validateWcExists(updateReqVO.getWcId());
        // 更新
        ResDO updateObj = BeanUtils.toBean(updateReqVO, ResDO.class);
        resMapper.updateById(updateObj);
    }

    @Override
    public void deleteRes(Long id) {
        // 校验存在
        validateResExists(id);
        // 校验引用：已配置能力数据的资源不允许删除
        if (!resCapabilityMapper.selectListByResId(id).isEmpty()) {
            throw exception(RES_DELETE_REFERENCED);
        }
        // 删除
        resMapper.deleteById(id);
    }

    @Override
    public void deleteResListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteRes(id);
        }
    }

    private void validateResExists(Long id) {
        if (resMapper.selectById(id) == null) {
            throw exception(RES_NOT_EXISTS);
        }
    }

    private void validateWcExists(Long wcId) {
        if (wcMapper.selectById(wcId) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    @Override
    public ResRespVO getRes(Long id) {
        return resMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ResRespVO> getResPage(ResPageReqVO pageReqVO) {
        return resMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ResDO> getResList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return resMapper.selectByIds(ids);
    }

}
