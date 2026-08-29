package com.iboot.pf.module.mdm.service.rescapability;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilitySaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CAPABILITY_DUPLICATE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ResCapabilityServiceImpl.class)
class ResCapabilityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ResCapabilityService resCapabilityService;
    @Resource
    private WcMapper wcMapper;
    @Resource
    private OpMapper opMapper;
    @Resource
    private ResMapper resMapper;

    @BeforeEach
    void createReferences() {
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-1").name("加工中心").status(0).build());
        opMapper.insert(OpDO.builder().id(1L).code("OP-1").name("车削")
                .stdDuration(10).durationType("FIXED").status(0).build());
        opMapper.insert(OpDO.builder().id(2L).code("OP-2").name("铣削")
                .stdDuration(20).durationType("FIXED").status(0).build());
        resMapper.insert(ResDO.builder().id(1L).wcId(1L).resType("MACHINE").code("RES-1")
                .name("车床01").status(0).build());
    }

    @Test
    void testCreateResCapability_sameResAndOpRejected() {
        Long firstId = resCapabilityService.createResCapability(buildReqVO());
        assertNotNull(firstId);

        assertServiceException(
                () -> resCapabilityService.createResCapability(buildReqVO()), RES_CAPABILITY_DUPLICATE);
    }

    @Test
    void testCreateResCapability_missingResRejected() {
        ResCapabilitySaveReqVO reqVO = buildReqVO();
        reqVO.setResId(999L);
        assertServiceException(() -> resCapabilityService.createResCapability(reqVO), RES_NOT_EXISTS);
    }

    @Test
    void testUpdateResCapability_allowsDifferentOp() {
        Long id = resCapabilityService.createResCapability(buildReqVO());

        ResCapabilitySaveReqVO updateReqVO = buildReqVO();
        updateReqVO.setId(id);
        updateReqVO.setOpId(2L);
        resCapabilityService.updateResCapability(updateReqVO);

        assertEquals(2L, resCapabilityService.getResCapability(id).getOpId());
    }

    private ResCapabilitySaveReqVO buildReqVO() {
        ResCapabilitySaveReqVO reqVO = new ResCapabilitySaveReqVO();
        reqVO.setResId(1L);
        reqVO.setOpId(1L);
        reqVO.setPreferredFlag(true);
        reqVO.setEfficiency(new BigDecimal("1.2000"));
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
