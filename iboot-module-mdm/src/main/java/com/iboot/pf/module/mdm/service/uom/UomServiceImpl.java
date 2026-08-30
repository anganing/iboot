package com.iboot.pf.module.mdm.service.uom;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomSaveReqVO;
import com.iboot.pf.module.mdm.convert.uom.UomConvert;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.mysql.uom.UomMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

@Service
public class UomServiceImpl implements UomService {
    @Resource
    private UomMapper uomMapper;

    @Override
    public Long create(UomSaveReqVO reqVO) {
        UomDO data = UomConvert.INSTANCE.convert(reqVO);
        data.setId(null);
        normalizeCode(data);
        uomMapper.insert(data);
        return data.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UomSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        UomDO data = UomConvert.INSTANCE.convert(reqVO);
        normalizeCode(data);
        uomMapper.updateById(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validateExists(id);
        uomMapper.deleteById(id);
    }

    @Override
    public UomDO get(Long id) {
        return uomMapper.selectById(id);
    }

    @Override
    public PageResult<UomDO> getPage(UomPageReqVO reqVO) {
        return uomMapper.selectPage(reqVO);
    }

    @Override
    public List<UomDO> getUomList() {
        return uomMapper.selectList();
    }

    @Override
    public List<UomDO> getUomList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return uomMapper.selectByIds(ids);
    }

    private void validateExists(Long id) {
        if (uomMapper.selectById(id) == null) throw exception(UOM_NOT_EXISTS);
    }

    private void normalizeCode(UomDO data) {
        data.setCode(StrUtil.toUpperCase(StrUtil.trim(data.getCode())));
    }
}
