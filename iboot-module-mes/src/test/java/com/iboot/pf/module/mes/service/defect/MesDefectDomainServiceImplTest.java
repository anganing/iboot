package com.iboot.pf.module.mes.service.defect;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.defectcode.DefectCodeApi;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDefectLevelEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.defect.bo.MesCreateDefectBO;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_CODE_NOT_AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 缺陷录入领域动作测试。
 *
 * <p>覆盖任务书 T2-06 的过滤与落库门禁：物料分类过滤、工序过滤、通用码可选、越界缺陷码抛错。
 * REJECTED 无缺陷抛错在 {@code MesQcTaskDomainServiceImplTest}（校验改动的是提交结论动作，
 * 用例归属该测试类）。
 *
 * <p>过滤矩阵实现在 mdm 的 {@link DefectCodeApi#getAvailableDefectCodeList}（两张适用范围表
 * 是 mdm 的主数据，MES 不直查），本类 mock 门面契约验证 MES 侧的上下文解析与落库门禁，
 * mdm 侧 {@code DefectCodeApiTest} 有过滤专项用例；落库委托 codegen 的 {@code DefectService}
 * （快照补全、结果归属校验、附件替换都在它那里），本类验证转调契约。
 */
@Import(MesDefectDomainServiceImpl.class)
class MesDefectDomainServiceImplTest extends BaseDbUnitTest {

    private static final Long TASK_ID = 1L;
    private static final Long MAT_ID = 10L;
    private static final Long MAT_CAT_ID = 100L;
    private static final Long OP_ID = 200L;
    private static final Long CODE_LIMITED_BY_MAT_CAT = 51L;
    private static final Long CODE_LIMITED_BY_OP = 52L;
    private static final Long CODE_UNIVERSAL = 53L;

    @Resource
    private MesDefectDomainService defectDomainService;
    @Resource
    private DefectMapper defectMapper;

    @MockitoBean
    private DefectCodeApi defectCodeApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private QcTaskService qcTaskService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private DefectService defectService;

    @BeforeEach
    void setUp() {
        MaterialRespDTO material = MesTestFixtures.material();
        material.setId(MAT_ID);
        material.setCatId(MAT_CAT_ID);
        lenient().when(materialApi.getMaterial(MAT_ID)).thenReturn(material);
        // 任务桩：缺陷挂在质检任务上，物料过滤维度取任务快照的 matId
        lenient().when(qcTaskService.validateQcTaskExists(TASK_ID)).thenReturn(new QcTaskDO()
                .setId(TASK_ID).setMatId(MAT_ID).setStatus(MesQcTaskStatusEnum.INSPECTING.name()));
        // 缺陷码按编号取值（越界报错时拿编码拼提示），门面 Map 缺失编号不产生键的语义由调用方兜底
        lenient().when(snapshotService.requireDefectCode(anyLong())).thenAnswer(inv -> {
            DefectCodeRespDTO dto = new DefectCodeRespDTO();
            dto.setId(inv.getArgument(0));
            dto.setCode("DEF-" + inv.getArgument(0));
            dto.setLevel(MesDefectLevelEnum.MAJOR.name());
            return dto;
        });
    }

    // ========== 可选缺陷码：上下文解析 ==========

    @Test
    void testGetAvailableDefectCodes_matCatAndOpResolvedToApi() {
        defectDomainService.getAvailableDefectCodes(MAT_ID, OP_ID);

        // 物料解析出分类后连同工序一起交给门面，过滤矩阵由 mdm 侧实现
        verify(defectCodeApi).getAvailableDefectCodeList(MAT_CAT_ID, OP_ID);
    }

    @Test
    void testGetAvailableDefectCodes_noMatReturnsNullContext() {
        defectDomainService.getAvailableDefectCodes(null, OP_ID);

        verify(defectCodeApi).getAvailableDefectCodeList(null, OP_ID);
    }

    // ========== 缺陷录入 ==========

    private MesCreateDefectBO buildBO(Long defectCodeId) {
        MesCreateDefectBO bo = new MesCreateDefectBO();
        bo.setTaskId(TASK_ID);
        bo.setDefectCodeId(defectCodeId);
        bo.setOpId(OP_ID);
        bo.setQty(new BigDecimal("2"));
        bo.setDescription("绕线端部松动");
        return bo;
    }

    /** 打桩"分类 100 + 工序 200 下的可选范围"，返回打桩内容供断言。 */
    private List<DefectCodeRespDTO> stubAvailable(Long... ids) {
        List<DefectCodeRespDTO> available = Arrays.stream(ids).map(id -> {
            DefectCodeRespDTO dto = new DefectCodeRespDTO();
            dto.setId(id);
            dto.setCode("DEF-" + id);
            return dto;
        }).toList();
        when(defectCodeApi.getAvailableDefectCodeList(MAT_CAT_ID, OP_ID)).thenReturn(available);
        return available;
    }

    @Test
    void testCreateDefect_universalCodeAllowed() {
        // 通用码（两张关联表都无记录）任何场景可选
        stubAvailable(CODE_UNIVERSAL);
        when(defectService.createDefect(any())).thenReturn(900L);

        Long id = defectDomainService.createDefect(buildBO(CODE_UNIVERSAL));

        assertEquals(900L, id);
    }

    @Test
    void testCreateDefect_codeInScopeAllowed() {
        stubAvailable(CODE_LIMITED_BY_MAT_CAT, CODE_LIMITED_BY_OP, CODE_UNIVERSAL);
        when(defectService.createDefect(any())).thenReturn(901L);

        assertNotNull(defectDomainService.createDefect(buildBO(CODE_LIMITED_BY_MAT_CAT)));
    }

    @Test
    void testCreateDefect_codeOutOfScopeRejected() {
        // 可选范围只有通用码，限分类/限工序的代码越界被拒
        stubAvailable(CODE_UNIVERSAL);

        assertServiceException(() -> defectDomainService.createDefect(buildBO(CODE_LIMITED_BY_MAT_CAT)),
                DEFECT_CODE_NOT_AVAILABLE, "DEF-" + CODE_LIMITED_BY_MAT_CAT);
    }

    @Test
    void testCreateDefect_codeDisabledRejected() {
        // 可选范围一律来自启用代码：停用的代码即便编号合法也不在范围内
        stubAvailable(CODE_UNIVERSAL);

        assertServiceException(() -> defectDomainService.createDefect(buildBO(601L)),
                DEFECT_CODE_NOT_AVAILABLE, "DEF-601");
    }

    @Test
    void testCreateDefect_mapsAllFieldsToSaveReq() {
        stubAvailable(CODE_UNIVERSAL);
        MesCreateDefectBO bo = buildBO(CODE_UNIVERSAL);
        bo.setResultId(MesConstants.NONE_ID);
        bo.setLocation("A 面第三槽");
        bo.setRemark("单测数据");
        when(defectService.createDefect(any())).thenReturn(902L);

        defectDomainService.createDefect(bo);

        ArgumentCaptor<com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO> captor =
                ArgumentCaptor.forClass(
                        com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO.class);
        verify(defectService).createDefect(captor.capture());
        com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO reqVO = captor.getValue();
        assertEquals(TASK_ID, reqVO.getTaskId());
        assertEquals(MesConstants.NONE_ID, reqVO.getResultId());
        assertEquals(CODE_UNIVERSAL, reqVO.getDefectCodeId());
        assertEquals(0, reqVO.getQty().compareTo(new BigDecimal("2")));
        assertEquals("绕线端部松动", reqVO.getDescription());
        assertEquals("A 面第三槽", reqVO.getLocation());
        assertEquals("单测数据", reqVO.getRemark());
    }

}
