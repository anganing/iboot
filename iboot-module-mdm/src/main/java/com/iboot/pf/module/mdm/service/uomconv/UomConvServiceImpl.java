package com.iboot.pf.module.mdm.service.uomconv;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.UomConvPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.UomConvRespVO;
import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.UomConvSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.dal.mysql.uomconv.UomConvMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 单位换算 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class UomConvServiceImpl implements UomConvService {

    @Resource
    private UomConvMapper uomConvMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long createUomConv(UomConvSaveReqVO createReqVO) {
        validateReferences(createReqVO.getMatId(), createReqVO.getFromUomId(), createReqVO.getToUomId());
        // 插入
        UomConvDO uomConv = BeanUtils.toBean(createReqVO, UomConvDO.class);
        uomConv.setId(null);
        uomConvMapper.insert(uomConv);

        // 返回
        return uomConv.getId();
    }

    @Override
    public void updateUomConv(UomConvSaveReqVO updateReqVO) {
        // 校验存在
        validateUomConvExists(updateReqVO.getId());
        validateReferences(updateReqVO.getMatId(), updateReqVO.getFromUomId(), updateReqVO.getToUomId());
        // 更新
        UomConvDO updateObj = BeanUtils.toBean(updateReqVO, UomConvDO.class);
        uomConvMapper.updateById(updateObj);
    }

    @Override
    public void deleteUomConv(Long id) {
        // 校验存在
        validateUomConvExists(id);
        // 删除
        uomConvMapper.deleteById(id);
    }

    @Override
    public void deleteUomConvListByIds(List<Long> ids) {
        // 删除
        uomConvMapper.deleteByIds(ids);
    }


    private void validateUomConvExists(Long id) {
        if (uomConvMapper.selectById(id) == null) {
            throw exception(UOM_CONV_NOT_EXISTS);
        }
    }

    private void validateReferences(Long matId, Long fromUomId, Long toUomId) {
        if (matMapper.selectById(matId) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (uomMapper.selectById(fromUomId) == null || uomMapper.selectById(toUomId) == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        if (fromUomId.equals(toUomId)) {
            throw exception(UOM_CONV_SAME_UOM);
        }
    }

    @Override
    public UomConvRespVO getUomConv(Long id) {
        return uomConvMapper.selectJoinById(id);
    }

    @Override
    public PageResult<UomConvRespVO> getUomConvPage(UomConvPageReqVO pageReqVO) {
        return uomConvMapper.selectPage(pageReqVO);
    }

    @Override
    public List<UomConvDO> getUomConvList(Collection<Long> matIds) {
        if (CollUtil.isEmpty(matIds)) {
            return List.of();
        }
        return uomConvMapper.selectList(UomConvDO::getMatId, matIds);
    }

}
