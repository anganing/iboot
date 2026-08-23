package com.iboot.pf.module.mdm.service.matcat;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.matcat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 物料分类 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class MatCatServiceImpl implements MatCatService {

    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private com.iboot.pf.module.mdm.service.coderule.MdmCodeService mdmCodeService;

    @Override
    public Long createMatCat(MatCatSaveReqVO createReqVO) {
        // 校验父级编号的有效性
        validateParentMatCat(null, createReqVO.getParentId());
        // 校验名称的唯一性
        validateMatCatNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        MatCatDO matCat = BeanUtils.toBean(createReqVO, MatCatDO.class);
        matCat.setId(null);
        matCat.setCode(mdmCodeService.resolveCode(matCat.getCode(), "MDM_MAT_CAT_CODE"));
        matCatMapper.insert(matCat);

        // 返回
        return matCat.getId();
    }

    @Override
    public void updateMatCat(MatCatSaveReqVO updateReqVO) {
        // 校验存在
        validateMatCatExists(updateReqVO.getId());
        // 校验父级编号的有效性
        validateParentMatCat(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验名称的唯一性
        validateMatCatNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        MatCatDO updateObj = BeanUtils.toBean(updateReqVO, MatCatDO.class);
        matCatMapper.updateById(updateObj);
    }

    @Override
    public void deleteMatCat(Long id) {
        // 校验存在
        validateMatCatExists(id);
        // 校验是否有子物料分类
        if (matCatMapper.selectCountByParentId(id) > 0) {
            throw exception(MAT_CAT_EXITS_CHILDREN);
        }
        // 删除
        matCatMapper.deleteById(id);
    }


    private void validateMatCatExists(Long id) {
        if (matCatMapper.selectById(id) == null) {
            throw exception(MAT_CAT_NOT_EXISTS);
        }
    }

    private void validateParentMatCat(Long id, Long parentId) {
        if (parentId == null || MatCatDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父物料分类
        if (Objects.equals(id, parentId)) {
            throw exception(MAT_CAT_PARENT_ERROR);
        }
        // 2. 父物料分类不存在
        MatCatDO parentMatCat = matCatMapper.selectById(parentId);
        if (parentMatCat == null) {
            throw exception(MAT_CAT_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父物料分类，如果父物料分类是自己的子物料分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentMatCat.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(MAT_CAT_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父物料分类
            if (parentId == null || MatCatDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentMatCat = matCatMapper.selectById(parentId);
            if (parentMatCat == null) {
                break;
            }
        }
    }

    private void validateMatCatNameUnique(Long id, Long parentId, String name) {
        MatCatDO matCat = matCatMapper.selectByParentIdAndName(parentId, name);
        if (matCat == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的物料分类
        if (id == null) {
            throw exception(MAT_CAT_NAME_DUPLICATE);
        }
        if (!Objects.equals(matCat.getId(), id)) {
            throw exception(MAT_CAT_NAME_DUPLICATE);
        }
    }

    @Override
    public MatCatDO getMatCat(Long id) {
        return matCatMapper.selectById(id);
    }

    @Override
    public List<MatCatDO> getMatCatList(MatCatListReqVO listReqVO) {
        return matCatMapper.selectList(listReqVO);
    }

}