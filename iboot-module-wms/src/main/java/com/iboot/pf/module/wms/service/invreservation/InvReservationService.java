package com.iboot.pf.module.wms.service.invreservation;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;

/**
 * WMS-库存预留 codegen Service 接口。
 *
 * <p><b>只读</b>：预留的建立、释放、核销一律走领域侧
 * {@code service.reservation.WmsInventoryReservationService}。原因是 codegen 侧那套 CRUD 只往
 * {@code wms_inv_reservation} 插记录、不改 {@code wms_inv_balance.qty_reserved}，
 * 可用量虚高、超发无人拦。这里保留的只有 Mapper XML 支撑的 JOIN 展示查询与物料名批量补全。
 *
 * @author iboot
 */
public interface InvReservationService {

    /**
     * 获得WMS-库存预留
     *
     * @param id 编号
     * @return WMS-库存预留
     */
    InvReservationRespVO getInvReservation(Long id);

    /**
     * 获得WMS-库存预留分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存预留分页
     */
    PageResult<InvReservationRespVO> getInvReservationPage(InvReservationPageReqVO pageReqVO);

}
