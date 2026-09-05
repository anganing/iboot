package com.iboot.pf.module.mdm.api.bom;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.bom.dto.BomDetailRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 物料清单（BOM）API 实现类
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class BomApiImpl implements BomApi {

    @Resource
    private BomService bomService;
    @Resource
    private MaterialApi materialApi;

    @Override
    public BomRespDTO getBom(Long id) {
        BomDO bom = bomService.getBomDO(id);
        return BeanUtils.toBean(bom, BomRespDTO.class);
    }

    @Override
    public BomRespDTO getEffectiveBom(Long matId, LocalDate date) {
        BomDO bom = bomService.getEffectiveBom(matId, date);
        return BeanUtils.toBean(bom, BomRespDTO.class);
    }

    @Override
    public BomDetailRespDTO getBomDetail(Long id) {
        BomDO bom = bomService.getBomDO(id);
        if (bom == null) {
            return null;
        }
        BomDetailRespDTO detail = BeanUtils.toBean(bom, BomDetailRespDTO.class);
        detail.setLines(getBomLineList(id));
        detail.setMat(materialApi.getMaterial(bom.getMatId()));
        return detail;
    }

    @Override
    public List<BomLineRespDTO> getBomLineList(Long bomId) {
        List<BomLineDO> lines = bomService.getBomLineList(bomId);
        return BeanUtils.toBean(lines, BomLineRespDTO.class);
    }

}
