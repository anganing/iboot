package com.iboot.pf.module.wms.service.arrival;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
import com.iboot.pf.module.wms.dal.mysql.arrivalline.ArrivalLineMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * WMS-到货登记 codegen Service 实现类。
 *
 * <p>只读：写入方法已由 T0-17 全部删除，见 {@link ArrivalService} 与 package-info 的分工说明。
 *
 * @author iboot
 */
@Service
@Validated
public class ArrivalServiceImpl implements ArrivalService {

    @Resource
    private ArrivalMapper arrivalMapper;
    @Resource
    private ArrivalLineMapper arrivalLineMapper;

    @Override
    public ArrivalRespVO getArrival(Long id) {
        return arrivalMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ArrivalRespVO> getArrivalPage(ArrivalPageReqVO pageReqVO) {
        return arrivalMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（WMS-到货登记明细） ====================

    @Override
    public PageResult<ArrivalLineRespVO> getArrivalLinePage(ArrivalLinePageReqVO pageReqVO) {
        return arrivalLineMapper.selectJoinPage(pageReqVO);
    }

    @Override
    public ArrivalLineRespVO getArrivalLine(Long id) {
        return arrivalLineMapper.selectJoinById(id);
    }

}
