package com.iboot.pf.server.integration.fixture;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.controller.admin.loc.vo.LocSaveReqVO;
import com.iboot.pf.module.wms.controller.admin.wh.vo.WhSaveReqVO;
import com.iboot.pf.module.wms.controller.admin.zone.vo.ZoneSaveReqVO;
import com.iboot.pf.module.wms.service.loc.LocService;
import com.iboot.pf.module.wms.service.wh.WhService;
import com.iboot.pf.module.wms.service.zone.ZoneService;
import com.iboot.pf.server.integration.PilotLine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * WMS 仓储数据工厂（T6-01）。
 *
 * 仓库、库区、库位走各自 create Service；**初始库存一律经 {@link InOrderApi} 真实过账建立**
 * （任务书明确"不直接插余额表"），因此库存余额、`wms_inv_txn` 流水与单据状态三者
 * 都是生产写入路径产出的真实事实，不是 fixture 手搓的假数据。
 *
 * @author iboot
 */
@Component
public class WmsFixture {

    /** 初始库存基数：后续领料用例按"领 100，500 → 400"设计，这里落 500。 */
    public static final BigDecimal INITIAL_QTY = new BigDecimal("500");

    @Resource
    private WhService whService;
    @Resource
    private ZoneService zoneService;
    @Resource
    private LocService locService;
    @Resource
    private InOrderApi inOrderApi;

    /**
     * 建仓库 + 库区 + 库位，并以真实过账建初始库存。
     *
     * <p>过账物料取成品的第一个子件（{@link PilotLine#anyComponentMatId()}）：
     * 它是 BOM 上的 PICK 件，领退料用例占的正是它的库存。
     *
     * @param line 试点产线句柄（用其 invOrgId / plantId / 子件物料 / 计量单位）
     * @return 仓库、库区、库位与已过账的入库单编号
     */
    public WmsStock createWarehouseWithStock(PilotLine line) {
        WhSaveReqVO wh = new WhSaveReqVO();
        wh.setInvOrgId(line.invOrgId());
        wh.setPlantId(line.plantId());
        wh.setCode("IT-WH-FG"); // wms_wh.code 必填（@NotEmpty）
        wh.setName("集成测试成品仓");
        wh.setWhType("FLAT");
        wh.setManagedBy("MANUAL");
        wh.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long whId = whService.createWh(wh);

        ZoneSaveReqVO zone = new ZoneSaveReqVO();
        zone.setWhId(whId);
        zone.setCode("IT-ZONE-RAW"); // wms_zone.code 必填（@NotEmpty）
        zone.setName("集成测试原料区");
        zone.setZoneType("RAW");
        zone.setAllowMixedMat(false);
        zone.setAllowMixedLot(false);
        zone.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long zoneId = zoneService.createZone(zone);

        LocSaveReqVO loc = new LocSaveReqVO();
        loc.setWhId(whId);
        loc.setZoneId(zoneId);
        loc.setCode("IT-LOC-RAW-01"); // wms_loc.code 必填（@NotEmpty）
        loc.setLocType("FLAT_AREA");
        loc.setDimensionCheckMode("NONE");
        loc.setStatus("EMPTY");
        Long locId = locService.createLoc(loc);

        // 初始库存：创建入库单（PO_IN）→ 过账；余额与流水由过账产生，不手工插表
        List<Long> inOrderIds = new ArrayList<>();
        Long inOrderId = inOrderApi.createInOrder(buildInitialInOrder(line, whId, locId));
        inOrderApi.confirmInOrder(buildConfirm(inOrderId));
        inOrderIds.add(inOrderId);

        return new WmsStock(whId, zoneId, locId, inOrderIds);
    }

    private CreateInOrderReqDTO buildInitialInOrder(PilotLine line, Long whId, Long locId) {
        CreateInOrderReqDTO req = new CreateInOrderReqDTO();
        req.setSourceModule("INTEGRATION");
        req.setSourceBizNo("T6-01-INIT-STOCK");
        req.setIdempotencyKey("INTEGRATION:T6-01-INIT-STOCK:CREATE_IN:" + whId);
        req.setBizType("PO_IN");
        req.setInvOrgId(line.invOrgId());
        req.setPlantId(line.plantId());
        req.setWhId(whId);
        req.setQualityStatus("QUALIFIED");

        CreateInOrderReqDTO.Line inLine = new CreateInOrderReqDTO.Line();
        inLine.setLineNo(10);
        inLine.setMatId(line.anyComponentMatId());
        inLine.setUomId(line.uomId());
        inLine.setUomCode(MdmFixture.UOM_CODE); // wms_in_order_line.uom_code 为 NOT NULL，调用方快照字段
        inLine.setLocId(locId);
        inLine.setPlanQty(INITIAL_QTY);
        req.setLines(List.of(inLine));
        return req;
    }

    private ConfirmOrderReqDTO buildConfirm(Long orderId) {
        ConfirmOrderReqDTO confirm = new ConfirmOrderReqDTO();
        confirm.setSourceModule("INTEGRATION");
        confirm.setSourceBizNo("T6-01-INIT-STOCK");
        confirm.setIdempotencyKey("INTEGRATION:T6-01-INIT-STOCK:CONFIRM_IN:" + orderId);
        confirm.setOrderId(orderId);
        return confirm;
    }

    /** 仓储建立结果：供用例回填 {@link PilotLine} 的 WMS 段或直接断言。 */
    public record WmsStock(Long whId, Long zoneId, Long locId, List<Long> inOrderIds) {
    }

}
