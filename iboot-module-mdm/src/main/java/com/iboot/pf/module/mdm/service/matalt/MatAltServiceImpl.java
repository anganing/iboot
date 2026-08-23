package com.iboot.pf.module.mdm.service.matalt;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.matalt.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.matalt.MatAltMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 替代料 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class MatAltServiceImpl implements MatAltService {

    @Resource
    private MatAltMapper matAltMapper;
    @Resource
    private MatMapper matMapper;

    @Override
    public Long createMatAlt(MatAltSaveReqVO createReqVO) {
        validateReferences(createReqVO.getMatId(), createReqVO.getAltMatId());
        // 插入
        MatAltDO matAlt = BeanUtils.toBean(createReqVO, MatAltDO.class);
        matAlt.setId(null);
        matAltMapper.insert(matAlt);

        // 返回
        return matAlt.getId();
    }

    @Override
    public void updateMatAlt(MatAltSaveReqVO updateReqVO) {
        // 校验存在
        validateMatAltExists(updateReqVO.getId());
        validateReferences(updateReqVO.getMatId(), updateReqVO.getAltMatId());
        // 更新
        MatAltDO updateObj = BeanUtils.toBean(updateReqVO, MatAltDO.class);
        matAltMapper.updateById(updateObj);
    }

    @Override
    public void deleteMatAlt(Long id) {
        // 校验存在
        validateMatAltExists(id);
        // 删除
        matAltMapper.deleteById(id);
    }

    @Override
        public void deleteMatAltListByIds(List<Long> ids) {
        // 删除
        matAltMapper.deleteByIds(ids);
        }


    private void validateMatAltExists(Long id) {
        if (matAltMapper.selectById(id) == null) {
            throw exception(MAT_ALT_NOT_EXISTS);
        }
    }

    private void validateReferences(Long matId, Long altMatId) {
        if (matMapper.selectById(matId) == null || matMapper.selectById(altMatId) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (matId.equals(altMatId)) {
            throw exception(MAT_ALT_SAME_MAT);
        }
    }

    @Override
    public MatAltDO getMatAlt(Long id) {
        return matAltMapper.selectById(id);
    }

    @Override
    public PageResult<MatAltDO> getMatAltPage(MatAltPageReqVO pageReqVO) {
        return matAltMapper.selectPage(pageReqVO);
    }

}
