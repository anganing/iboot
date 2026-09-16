package com.iboot.pf.module.mdm.api.uom;

import com.iboot.pf.framework.common.util.collection.CollectionUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.uom.dto.UomConvRespDTO;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import com.iboot.pf.module.mdm.service.uom.UomService;
import com.iboot.pf.module.mdm.service.uomconv.UomConvService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 计量单位与换算 API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class UomApiImpl implements UomApi {

    @Resource
    private UomService uomService;
    @Resource
    private UomConvService uomConvService;

    @Override
    public List<UomRespDTO> getUomList() {
        List<UomDO> uoms = uomService.getUomList();
        return BeanUtils.toBean(uoms, UomRespDTO.class);
    }

    @Override
    public Map<Long, UomRespDTO> getUomMap(Collection<Long> ids) {
        List<UomDO> uoms = uomService.getUomList(ids);
        List<UomRespDTO> list = BeanUtils.toBean(uoms, UomRespDTO.class);
        return CollectionUtils.convertMap(list, UomRespDTO::getId);
    }

    @Override
    public Map<String, UomRespDTO> getUomMapByCodes(Collection<String> codes) {
        List<UomDO> uoms = uomService.getUomListByCodes(codes);
        List<UomRespDTO> list = BeanUtils.toBean(uoms, UomRespDTO.class);
        return CollectionUtils.convertMap(list, UomRespDTO::getCode);
    }

    @Override
    public List<UomConvRespDTO> getUomConvList(Collection<Long> matIds) {
        List<UomConvDO> convs = uomConvService.getUomConvList(matIds);
        return BeanUtils.toBean(convs, UomConvRespDTO.class);
    }

}
