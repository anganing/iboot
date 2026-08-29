package com.iboot.pf.module.mdm.service.changeover;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CHANGEOVER_DUPLICATE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.RES_CHANGEOVER_SAME_GROUP;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ResChangeoverServiceImpl.class)
class ResChangeoverServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ResChangeoverService resChangeoverService;
    @Resource
    private ResMapper resMapper;
    @Resource
    private WcMapper wcMapper;

    @BeforeEach
    void createReferences() {
        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-1").name("喷涂中心").status(0).build());
        resMapper.insert(ResDO.builder().id(1L).wcId(1L).resType("MACHINE").code("RES-1")
                .name("喷涂机01").status(0).build());
    }

    @Test
    void testCreateResChangeover_sameResAndGroupsRejected() {
        resChangeoverService.createResChangeover(buildReqVO());
        assertServiceException(() -> resChangeoverService.createResChangeover(buildReqVO()),
                RES_CHANGEOVER_DUPLICATE);
    }

    @Test
    void testCreateResChangeover_sameGroupRejected() {
        ResChangeoverSaveReqVO reqVO = buildReqVO();
        reqVO.setToGroup("红色");
        assertServiceException(() -> resChangeoverService.createResChangeover(reqVO), RES_CHANGEOVER_SAME_GROUP);
    }

    @Test
    void testCreateResChangeover_byResGroupOnly() {
        ResChangeoverSaveReqVO reqVO = buildReqVO();
        reqVO.setResId(0L);
        reqVO.setResGroup("SPRAY");
        Long id = resChangeoverService.createResChangeover(reqVO);
        assertNotNull(id);
    }

    private ResChangeoverSaveReqVO buildReqVO() {
        ResChangeoverSaveReqVO reqVO = new ResChangeoverSaveReqVO();
        reqVO.setResId(1L);
        reqVO.setFromGroup("红色");
        reqVO.setToGroup("蓝色");
        reqVO.setChangeoverMinutes(30);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
