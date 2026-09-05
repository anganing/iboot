package com.iboot.pf.module.wms.service.invreservation;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.dal.mysql.reservation.WmsInvReservationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;

/**
 * WMS-库存预留 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link InvReservationService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class InvReservationServiceImpl implements InvReservationService {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private WmsInvReservationMapper invReservationMapper;

    @Override
    public InvReservationRespVO getInvReservation(Long id) {
        InvReservationRespVO reservation = invReservationMapper.selectJoinById(id);
        fillMaterialName(reservation == null ? Collections.emptyList() : Collections.singletonList(reservation));
        return reservation;
    }

    @Override
    public PageResult<InvReservationRespVO> getInvReservationPage(InvReservationPageReqVO pageReqVO) {
        PageResult<InvReservationRespVO> pageResult = invReservationMapper.selectPage(pageReqVO);
        fillMaterialName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全物料编码与名称。物料属于 mdm，只能走门面批量取，不能在 SQL 里跨库 JOIN；
     * 补全放在 Service 而不是 Controller，Excel 导出复用同一份结果才不会丢字段。
     */
    private void fillMaterialName(List<InvReservationRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<Long, MaterialRespDTO> materialMap =
                materialApi.getMaterialMap(convertSet(list, InvReservationRespVO::getMatId));
        list.forEach(vo -> findAndThen(materialMap, vo.getMatId(), material -> {
            vo.setMatCode(material.getCode());
            vo.setMatName(material.getName());
        }));
    }

}
