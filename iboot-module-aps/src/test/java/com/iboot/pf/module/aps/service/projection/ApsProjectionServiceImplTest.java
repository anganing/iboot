package com.iboot.pf.module.aps.service.projection;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsMaterialProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsMaterialProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.projection.source.OrderProjectionSource;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.resource.ResourceApi;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.uom.UomApi;
import com.iboot.pf.module.mdm.api.uom.dto.UomRespDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * APS 输入投影刷新测试。
 *
 * <p>空物料范围的语义是“刷新全部启用物料”，不是“刷新零条物料”。如果这里退化成
 * {@code getMaterialList(empty)}，场景页点“刷新输入”会把物料投影整表清空。
 */
@Import({ ApsProjectionServiceImpl.class, ApsCommandServiceImpl.class })
class ApsProjectionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsProjectionService projectionService;
    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsMaterialProjectionMapper materialProjectionMapper;

    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private UomApi uomApi;
    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private ResourceApi resourceApi;
    @MockitoBean
    private InventoryApi inventoryApi;
    @MockitoBean
    private ObjectProvider<OrderProjectionSource> orderSourceProvider;
    @MockitoBean
    private ApsRelationFiller relationFiller;

    @Test
    void refreshMaterialProjection_emptyIdsShouldProjectAllEnabledMaterials() {
        ApsScenarioDO scenario = ApsScenarioDO.builder()
                .code("APS-SC-001")
                .name("测试场景")
                .plantId(1L)
                .workshopId(0L)
                .strategyId(1L)
                .planStartTime(LocalDateTime.of(2026, 9, 1, 8, 0))
                .planEndTime(LocalDateTime.of(2026, 9, 2, 8, 0))
                .status(ApsScenarioStatusEnum.READY.name())
                .build();
        scenarioMapper.insert(scenario);

        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(100L);
        material.setCode("MAT-001");
        material.setName("测试物料");
        material.setMatType("RAW");
        material.setBaseUomId(10L);
        material.setBatchFlag(false);
        UomRespDTO uom = new UomRespDTO();
        uom.setId(10L);
        uom.setCode("PCS");

        when(materialApi.getEnabledMaterialList()).thenReturn(List.of(material));
        when(uomApi.getUomMap(any())).thenReturn(Map.of(10L, uom));

        RefreshProjectionReqDTO reqDTO = new RefreshProjectionReqDTO();
        reqDTO.setScenarioId(scenario.getId());
        reqDTO.setIdempotencyKey("TEST:REFRESH_MATERIAL:ALL");
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(scenario.getId()));

        projectionService.refreshMaterialProjection(reqDTO);

        List<ApsMaterialProjectionDO> rows = materialProjectionMapper.selectList(
                ApsMaterialProjectionDO::getScenarioId, scenario.getId());
        assertEquals(1, rows.size());
        assertEquals("MAT-001", rows.get(0).getMatCode());
        verify(materialApi).getEnabledMaterialList();
        verify(materialApi, never()).getMaterialList(any());
    }

}
