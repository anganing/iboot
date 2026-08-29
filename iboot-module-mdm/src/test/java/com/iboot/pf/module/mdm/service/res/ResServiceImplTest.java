package com.iboot.pf.module.mdm.service.res;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilitySaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.op.OpDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.op.OpMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.rescapability.ResCapabilityService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_DELETE_REFERENCED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({ResServiceImpl.class, com.iboot.pf.module.mdm.service.rescapability.ResCapabilityServiceImpl.class})
class ResServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ResService resService;
    @Resource
    private ResCapabilityService resCapabilityService;
    @Resource
    private ResMapper resMapper;
    @Resource
    private WcMapper wcMapper;
    @Resource
    private OpMapper opMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void createReferences() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-1").name("加工中心").status(0).build());
        opMapper.insert(OpDO.builder().id(1L).code("OP-1").name("车削")
                .stdDuration(10).durationType("FIXED").status(0).build());
    }

    @Test
    void testDeleteRes_referencedByCapability() {
        ResDO res = ResDO.builder().wcId(1L).resType("MACHINE").code("RES-1")
                .name("车床01").status(0).build();
        resMapper.insert(res);

        ResCapabilitySaveReqVO reqVO = new ResCapabilitySaveReqVO();
        reqVO.setResId(res.getId());
        reqVO.setOpId(1L);
        reqVO.setPreferredFlag(false);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        resCapabilityService.createResCapability(reqVO);

        assertServiceException(() -> resService.deleteRes(res.getId()), RES_DELETE_REFERENCED);
    }

}
