package com.iboot.pf.module.mdm.service.defectcode;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodePageReqVO;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeRespVO;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeMatCatDO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeOpDO;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeMatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.defectcode.DefectCodeOpMapper;
import com.iboot.pf.module.mdm.dal.mysql.matcat.MatCatMapper;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_HAS_CHILDREN;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_MAT_CAT_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_OP_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_PARENT_INVALID;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.DEFECT_CODE_PARENT_IS_CHILD;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.DEFECT_CODE;

/**
 * 缺陷代码 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DefectCodeServiceImpl implements DefectCodeService {

    @Resource
    private DefectCodeMapper defectCodeMapper;
    @Resource
    private DefectCodeOpMapper defectCodeOpMapper;
    @Resource
    private DefectCodeMatCatMapper defectCodeMatCatMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private MatCatMapper matCatMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDefectCode(DefectCodeSaveReqVO createReqVO) {
        validateParent(createReqVO.getParentId(), null);
        validateScopeReferences(createReqVO);
        DefectCodeDO defectCode = BeanUtils.toBean(createReqVO, DefectCodeDO.class);
        defectCode.setId(null);
        defectCode.setCode(mdmCodeService.resolveCode(defectCode.getCode(), DEFECT_CODE));
        defectCodeMapper.insert(defectCode);
        replaceScope(defectCode.getId(), createReqVO.getOpIds(), createReqVO.getMatCatIds());
        return defectCode.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefectCode(DefectCodeSaveReqVO updateReqVO) {
        DefectCodeDO existing = validateDefectCodeExists(updateReqVO.getId());
        validateParent(updateReqVO.getParentId(), existing.getId());
        validateScopeReferences(updateReqVO);
        DefectCodeDO updateObj = BeanUtils.toBean(updateReqVO, DefectCodeDO.class);
        defectCodeMapper.updateById(updateObj);
        replaceScope(existing.getId(), updateReqVO.getOpIds(), updateReqVO.getMatCatIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefectCode(Long id) {
        validateDefectCodeExists(id);
        if (!defectCodeMapper.selectListByParentId(id).isEmpty()) {
            throw exception(DEFECT_CODE_HAS_CHILDREN);
        }
        defectCodeOpMapper.deleteByDefectCodeIds(List.of(id));
        defectCodeMatCatMapper.deleteByDefectCodeIds(List.of(id));
        defectCodeMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefectCodeListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteDefectCode(id);
        }
    }

    private DefectCodeDO validateDefectCodeExists(Long id) {
        DefectCodeDO defectCode = defectCodeMapper.selectById(id);
        if (defectCode == null) {
            throw exception(DEFECT_CODE_NOT_EXISTS);
        }
        return defectCode;
    }

    private void validateScopeReferences(DefectCodeSaveReqVO reqVO) {
        if (reqVO.getOpIds() != null) {
            for (Long opId : reqVO.getOpIds()) {
                if (opMapper.selectById(opId) == null) {
                    throw exception(DEFECT_CODE_OP_NOT_EXISTS);
                }
            }
        }
        if (reqVO.getMatCatIds() != null) {
            for (Long matCatId : reqVO.getMatCatIds()) {
                if (matCatMapper.selectById(matCatId) == null) {
                    throw exception(DEFECT_CODE_MAT_CAT_NOT_EXISTS);
                }
            }
        }
    }

    /**
     * 适用范围全量替换：先清空再批量插入，无关联行即表示不限。
     */
    private void replaceScope(Long defectCodeId, List<Long> opIds, List<Long> matCatIds) {
        defectCodeOpMapper.deleteByDefectCodeIds(List.of(defectCodeId));
        defectCodeMatCatMapper.deleteByDefectCodeIds(List.of(defectCodeId));
        if (opIds != null && !opIds.isEmpty()) {
            defectCodeOpMapper.insertBatch(new LinkedHashSet<>(opIds).stream()
                    .map(opId -> DefectCodeOpDO.builder().defectCodeId(defectCodeId).opId(opId).build())
                    .toList());
        }
        if (matCatIds != null && !matCatIds.isEmpty()) {
            defectCodeMatCatMapper.insertBatch(new LinkedHashSet<>(matCatIds).stream()
                    .map(matCatId -> DefectCodeMatCatDO.builder()
                            .defectCodeId(defectCodeId).matCatId(matCatId).build())
                    .toList());
        }
    }

    private void validateParent(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        if (parentId.equals(selfId)) {
            throw exception(DEFECT_CODE_PARENT_INVALID);
        }
        DefectCodeDO parent = defectCodeMapper.selectById(parentId);
        if (parent == null) {
            throw exception(DEFECT_CODE_PARENT_INVALID);
        }
        // 防环：把自身链条上的祖先作为父级会形成环，向上遍历检测
        Long ancestorId = parent.getParentId();
        while (ancestorId != null && ancestorId != 0L) {
            if (ancestorId.equals(selfId)) {
                throw exception(DEFECT_CODE_PARENT_IS_CHILD);
            }
            DefectCodeDO ancestor = defectCodeMapper.selectById(ancestorId);
            ancestorId = ancestor == null ? null : ancestor.getParentId();
        }
    }

    @Override
    public DefectCodeRespVO getDefectCode(Long id) {
        return defectCodeMapper.selectJoinById(id);
    }

    @Override
    public PageResult<DefectCodeRespVO> getDefectCodePage(DefectCodePageReqVO pageReqVO) {
        return defectCodeMapper.selectJoinPage(pageReqVO);
    }

}
