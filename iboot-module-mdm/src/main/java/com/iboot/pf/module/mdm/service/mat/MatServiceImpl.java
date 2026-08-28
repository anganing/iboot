package com.iboot.pf.module.mdm.service.mat;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatRespVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.MAT;

/**
 * 物料主档 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class MatServiceImpl implements MatService {

    @Resource
    private MatMapper matMapper;
    @Resource
    private MdmCodeService mdmCodeService;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long createMat(MatSaveReqVO createReqVO) {
        validateReferences(createReqVO.getCatId(), createReqVO.getBaseUomId());
        // 插入
        MatDO mat = BeanUtils.toBean(createReqVO, MatDO.class);
        mat.setId(null);
        mat.setCode(mdmCodeService.resolveCode(mat.getCode(), MAT));
        matMapper.insert(mat);

        // 返回
        return mat.getId();
    }

    @Override
    public void updateMat(MatSaveReqVO updateReqVO) {
        // 校验存在
        validateMatExists(updateReqVO.getId());
        validateReferences(updateReqVO.getCatId(), updateReqVO.getBaseUomId());
        // 更新
        MatDO updateObj = BeanUtils.toBean(updateReqVO, MatDO.class);
        matMapper.updateById(updateObj);
    }

    @Override
    public void deleteMat(Long id) {
        // 校验存在
        validateMatExists(id);
        // 删除
        matMapper.deleteById(id);
    }

    @Override
        public void deleteMatListByIds(List<Long> ids) {
        // 删除
        matMapper.deleteByIds(ids);
        }


    private void validateMatExists(Long id) {
        if (matMapper.selectById(id) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
    }

    private void validateReferences(Long catId, Long baseUomId) {
        if (matCatMapper.selectById(catId) == null) {
            throw exception(MAT_CAT_NOT_EXISTS);
        }
        if (uomMapper.selectById(baseUomId) == null) {
            throw exception(UOM_NOT_EXISTS);
        }
    }

    @Override
    public MatRespVO getMat(Long id) {
        return matMapper.selectJoinById(id);
    }

    @Override
    public PageResult<MatRespVO> getMatPage(MatPageReqVO pageReqVO) {
        return matMapper.selectPage(pageReqVO);
    }

}
