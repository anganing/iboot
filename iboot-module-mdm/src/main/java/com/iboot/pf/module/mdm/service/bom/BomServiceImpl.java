package com.iboot.pf.module.mdm.service.bom;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.bom.BomMapper;
import com.iboot.pf.module.mdm.dal.mysql.bomline.BomLineMapper;
import com.iboot.pf.module.mdm.dal.mysql.mat.MatMapper;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 物料清单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class BomServiceImpl implements BomService {

    @Resource
    private BomMapper bomMapper;
    @Resource
    private BomLineMapper bomLineMapper;
    @Resource
    private MatMapper matMapper;
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long createBom(BomSaveReqVO createReqVO) {
        validateBomFields(createReqVO);
        // 插入
        BomDO bom = BeanUtils.toBean(createReqVO, BomDO.class);
        bom.setId(null);
        bomMapper.insert(bom);

        // 返回
        return bom.getId();
    }

    @Override
    public void updateBom(BomSaveReqVO updateReqVO) {
        BomDO bom = validateBomExists(updateReqVO.getId());
        validateBomEditable(bom);
        validateBomFields(updateReqVO);
        if ("PUBLISHED".equals(updateReqVO.getStatus()) && bomLineMapper.selectCountByBomId(bom.getId()) == 0) {
            throw exception(BOM_PUBLISH_WITHOUT_LINE);
        }
        // 更新
        BomDO updateObj = BeanUtils.toBean(updateReqVO, BomDO.class);
        bomMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long id) {
        BomDO bom = validateBomExists(id);
        validateBomEditable(bom);
        // 删除
        bomMapper.deleteById(id);

        // 删除子表
        deleteBomLineByBomId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteBomListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteBom(id);
        }
    }


    private BomDO validateBomExists(Long id) {
        BomDO bom = bomMapper.selectById(id);
        if (bom == null) {
            throw exception(BOM_NOT_EXISTS);
        }
        return bom;
    }

    private void validateBomFields(BomSaveReqVO reqVO) {
        if (matMapper.selectById(reqVO.getMatId()) == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (!Set.of("DRAFT", "PUBLISHED").contains(reqVO.getStatus())) {
            throw exception(BOM_STATUS_INVALID);
        }
        if (reqVO.getExpiryDate() != null && reqVO.getExpiryDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(BOM_DATE_INVALID);
        }
    }

    private void validateBomEditable(BomDO bom) {
        if ("PUBLISHED".equals(bom.getStatus())) {
            throw exception(BOM_PUBLISHED_LOCKED);
        }
    }

    @Override
    public BomDO getBom(Long id) {
        return bomMapper.selectById(id);
    }

    @Override
    public PageResult<BomDO> getBomPage(BomPageReqVO pageReqVO) {
        return bomMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（物料清单明细） ====================

    @Override
    public PageResult<BomLineDO> getBomLinePage(PageParam pageReqVO, Long bomId) {
        return bomLineMapper.selectPage(pageReqVO, bomId);
    }

    @Override
    public Long createBomLine(BomLineDO bomLine) {
        BomDO bom = validateBomExists(bomLine.getBomId());
        validateBomEditable(bom);
        populateBomLineMaterialSnapshot(bomLine);
        bomLine.clean(); // 清理掉创建、更新时间等相关属性值
        bomLine.setId(null);
        bomLineMapper.insert(bomLine);
        return bomLine.getId();
    }

    @Override
    public void updateBomLine(BomLineDO bomLine) {
        BomLineDO existing = validateBomLineExists(bomLine.getId());
        BomDO bom = validateBomExists(existing.getBomId());
        validateBomEditable(bom);
        bomLine.setBomId(existing.getBomId());
        populateBomLineMaterialSnapshot(bomLine);
        // 更新
        bomLine.clean(); // 解决更新情况下：updateTime 不更新
        bomLineMapper.updateById(bomLine);
    }

    @Override
    public void deleteBomLine(Long id) {
        BomLineDO bomLine = validateBomLineExists(id);
        validateBomEditable(validateBomExists(bomLine.getBomId()));
        bomLineMapper.deleteById(id);
    }

	@Override
	public void deleteBomLineListByIds(List<Long> ids) {
	    for (Long id : ids) {
            deleteBomLine(id);
        }
	}

    @Override
    public BomLineDO getBomLine(Long id) {
        return bomLineMapper.selectById(id);
    }

    private BomLineDO validateBomLineExists(Long id) {
        BomLineDO bomLine = bomLineMapper.selectById(id);
        if (bomLine == null) {
            throw exception(BOM_LINE_NOT_EXISTS);
        }
        return bomLine;
    }

    private void populateBomLineMaterialSnapshot(BomLineDO bomLine) {
        MatDO mat = matMapper.selectById(bomLine.getMatId());
        if (mat == null) {
            throw exception(MAT_NOT_EXISTS);
        }
        if (uomMapper.selectById(bomLine.getUomId()) == null) {
            throw exception(UOM_NOT_EXISTS);
        }
        bomLine.setMatCode(mat.getCode());
        bomLine.setMatName(mat.getName());
    }

    private void deleteBomLineByBomId(Long bomId) {
        bomLineMapper.deleteByBomId(bomId);
    }

	private void deleteBomLineByBomIds(List<Long> bomIds) {
        bomLineMapper.deleteByBomIds(bomIds);
	}

}
