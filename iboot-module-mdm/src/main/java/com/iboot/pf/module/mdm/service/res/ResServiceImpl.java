package com.iboot.pf.module.mdm.service.res;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.res.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

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
    private com.iboot.pf.module.mdm.service.coderule.MdmCodeService mdmCodeService;
    @Resource
    private WcMapper wcMapper;

    @Override
    public Long createRes(ResSaveReqVO createReqVO) {
        validateWcExists(createReqVO.getWcId());
        // 插入
        ResDO res = BeanUtils.toBean(createReqVO, ResDO.class);
        res.setId(null);
        res.setCode(mdmCodeService.resolveCode(res.getCode(), "MDM_RES_CODE"));
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
        // 删除
        resMapper.deleteById(id);
    }

    @Override
        public void deleteResListByIds(List<Long> ids) {
        // 删除
        resMapper.deleteByIds(ids);
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
    public ResDO getRes(Long id) {
        return resMapper.selectById(id);
    }

    @Override
    public PageResult<ResDO> getResPage(ResPageReqVO pageReqVO) {
        return resMapper.selectPage(pageReqVO);
    }

}
