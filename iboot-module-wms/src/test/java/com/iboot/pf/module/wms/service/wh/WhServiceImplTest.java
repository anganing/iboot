package com.iboot.pf.module.wms.service.wh;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.org.OrgApi;
import com.iboot.pf.module.mdm.api.org.dto.InvOrgRespDTO;
import com.iboot.pf.module.mdm.api.org.dto.PlantRespDTO;
import com.iboot.pf.module.wms.controller.admin.wh.vo.WhPageReqVO;
import com.iboot.pf.module.wms.controller.admin.wh.vo.WhRespVO;
import com.iboot.pf.module.wms.controller.admin.wh.vo.WhSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsWhDO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;
import com.iboot.pf.module.wms.dal.mysql.wh.WhMapper;
import com.iboot.pf.module.wms.dal.mysql.zone.ZoneMapper;
import com.iboot.pf.module.wms.enums.WmsManagedByEnum;
import com.iboot.pf.module.wms.enums.WmsWhTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_CODE_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_HAS_LOC;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_HAS_ZONE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_INV_ORG_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WH_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 仓库 Service 测试：编码自动分配、同组织编码唯一、跨模块组织校验、删除前的下级占用检查。
 */
@Import(WhServiceImpl.class)
class WhServiceImplTest extends BaseDbUnitTest {

    private static final Long INV_ORG = 1L;
    private static final Long PLANT = 1L;

    @Resource
    private WhService whService;
    @Resource
    private WhMapper whMapper;
    @Resource
    private ZoneMapper zoneMapper;
    @Resource
    private LocMapper locMapper;

    @MockitoBean
    private WmsCodeService codeService;
    @MockitoBean
    private OrgApi orgApi;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "WH0001" : invocation.getArgument(1));
        // 库存组织与工厂来自 mdm 门面，只有登记过的编号才算存在
        when(orgApi.getInvOrgMap(any())).thenAnswer(invocation -> buildInvOrgMap(invocation.getArgument(0)));
        when(orgApi.getPlantMap(any())).thenAnswer(invocation -> buildPlantMap(invocation.getArgument(0)));
    }

    private Map<Long, InvOrgRespDTO> buildInvOrgMap(Collection<Long> ids) {
        Map<Long, InvOrgRespDTO> result = new HashMap<>();
        ids.stream().filter(INV_ORG::equals).forEach(id -> {
            InvOrgRespDTO dto = new InvOrgRespDTO();
            dto.setId(id);
            dto.setCode("IO0001");
            dto.setName("默认库存组织");
            result.put(id, dto);
        });
        return result;
    }

    private Map<Long, PlantRespDTO> buildPlantMap(Collection<Long> ids) {
        Map<Long, PlantRespDTO> result = new HashMap<>();
        ids.stream().filter(PLANT::equals).forEach(id -> {
            PlantRespDTO dto = new PlantRespDTO();
            dto.setId(id);
            dto.setCode("PL0001");
            dto.setName("默认工厂");
            result.put(id, dto);
        });
        return result;
    }

    private WhSaveReqVO buildReqVO(String code) {
        WhSaveReqVO reqVO = new WhSaveReqVO();
        reqVO.setInvOrgId(INV_ORG);
        reqVO.setPlantId(PLANT);
        reqVO.setCode(code);
        reqVO.setName("原料立库");
        reqVO.setWhType(WmsWhTypeEnum.ASRS.name());
        reqVO.setManagedBy(WmsManagedByEnum.WCS.name());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    @Test
    void testCreateWh_blankCodeFilledByRule() {
        Long id = whService.createWh(buildReqVO(null));

        WmsWhDO wh = whMapper.selectById(id);
        assertNotNull(wh);
        assertEquals("WH0001", wh.getCode());
        assertEquals(WmsWhTypeEnum.ASRS.name(), wh.getWhType());
        assertEquals(WmsManagedByEnum.WCS.name(), wh.getManagedBy());
    }

    @Test
    void testCreateWh_duplicateCodeRejected() {
        whService.createWh(buildReqVO("WH-A"));

        assertServiceException(() -> whService.createWh(buildReqVO("WH-A")), WH_CODE_DUPLICATE);
    }

    @Test
    void testCreateWh_invalidInvOrgRejected() {
        WhSaveReqVO reqVO = buildReqVO("WH-A");
        reqVO.setInvOrgId(999L);

        assertServiceException(() -> whService.createWh(reqVO), WH_INV_ORG_NOT_EXISTS);
    }

    @Test
    void testUpdateWh_keepOwnCode() {
        Long id = whService.createWh(buildReqVO("WH-A"));

        // 改自己时编码没变，不应该撞到自己
        WhSaveReqVO updateReqVO = buildReqVO("WH-A");
        updateReqVO.setId(id);
        updateReqVO.setName("原料立库改名");
        whService.updateWh(updateReqVO);

        assertEquals("原料立库改名", whMapper.selectById(id).getName());
    }

    @Test
    void testUpdateWh_duplicateCodeRejected() {
        whService.createWh(buildReqVO("WH-A"));
        Long secondId = whService.createWh(buildReqVO("WH-B"));

        WhSaveReqVO updateReqVO = buildReqVO("WH-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> whService.updateWh(updateReqVO), WH_CODE_DUPLICATE);
    }

    @Test
    void testUpdateWh_notExists() {
        WhSaveReqVO updateReqVO = buildReqVO("WH-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> whService.updateWh(updateReqVO), WH_NOT_EXISTS);
    }

    @Test
    void testDeleteWh_success() {
        Long id = whService.createWh(buildReqVO("WH-A"));

        whService.deleteWh(id);

        assertNull(whMapper.selectById(id));
    }

    @Test
    void testDeleteWh_hasZoneRejected() {
        Long id = whService.createWh(buildReqVO("WH-A"));
        zoneMapper.insert(new WmsZoneDO().setWhId(id).setCode("ZN-1").setName("原料区")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        assertServiceException(() -> whService.deleteWh(id), WH_HAS_ZONE);
    }

    @Test
    void testDeleteWh_hasLocRejected() {
        Long id = whService.createWh(buildReqVO("WH-A"));
        locMapper.insert(new WmsLocDO().setWhId(id).setZoneId(0L).setCode("LOC-1"));

        assertServiceException(() -> whService.deleteWh(id), WH_HAS_LOC);
    }

    @Test
    void testDeleteWhList_hasZoneRejected() {
        Long first = whService.createWh(buildReqVO("WH-A"));
        Long second = whService.createWh(buildReqVO("WH-B"));
        zoneMapper.insert(new WmsZoneDO().setWhId(second).setCode("ZN-1").setName("原料区")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));

        // 批量删除同样要逐个检查下级占用，否则会留下孤儿库区
        assertServiceException(() -> whService.deleteWhListByIds(List.of(first, second)), WH_HAS_ZONE);
        assertNotNull(whMapper.selectById(first));
    }

    @Test
    void testGetWhPage_fillOrgName() {
        whService.createWh(buildReqVO("WH-A"));

        WhPageReqVO reqVO = new WhPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<WhRespVO> list = whService.getWhPage(reqVO).getList();

        assertEquals(1, list.size());
        // 组织名称由 Service 走 mdm 门面补全，Excel 导出复用同一结果才不会丢字段
        assertEquals("默认库存组织", list.get(0).getInvOrgName());
        assertEquals("默认工厂", list.get(0).getPlantName());
    }

}
