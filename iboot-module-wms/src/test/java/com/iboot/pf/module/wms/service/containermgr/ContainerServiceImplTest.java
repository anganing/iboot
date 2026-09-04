package com.iboot.pf.module.wms.service.containermgr;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.containermgr.vo.ContainerSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerDO;
import com.iboot.pf.module.wms.dal.mysql.container.WmsContainerMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsContainerStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 容器 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(ContainerServiceImpl.class)
class ContainerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ContainerService containerService;
    @Resource
    private WmsContainerMapper containerMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "CTN0001" : invocation.getArgument(1));
    }

    private ContainerSaveReqVO buildReqVO(String containerNo) {
        ContainerSaveReqVO reqVO = new ContainerSaveReqVO();
        reqVO.setContainerNo(containerNo);
        reqVO.setTypeId(1L);
        reqVO.setInvOrgId(1L);
        reqVO.setWhId(1L);
        reqVO.setLocId(1L);
        reqVO.setStatus(WmsContainerStatusEnum.EMPTY.name());
        reqVO.setVersion(0);
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateContainer_blankCodeFilledByRule() {
        Long id = containerService.createContainer(buildReqVO(null));

        WmsContainerDO entity = containerMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("CTN0001", entity.getContainerNo());
    }

    @Test
    void testCreateContainer_duplicateRejected() {
        containerService.createContainer(buildReqVO("CTN-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> containerService.createContainer(buildReqVO("CTN-A")), CONTAINER_NO_DUPLICATE);
    }

    @Test
    void testUpdateContainer_keepOwnCode() {
        Long id = containerService.createContainer(buildReqVO("CTN-A"));

        // 改自己时业务编码没变，不应该撞到自己
        ContainerSaveReqVO updateReqVO = buildReqVO("CTN-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        containerService.updateContainer(updateReqVO);

        assertEquals("规则调整", containerMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateContainer_duplicateRejected() {
        containerService.createContainer(buildReqVO("CTN-A"));
        Long secondId = containerService.createContainer(buildReqVO("CTN-B"));

        ContainerSaveReqVO updateReqVO = buildReqVO("CTN-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> containerService.updateContainer(updateReqVO), CONTAINER_NO_DUPLICATE);
    }

    @Test
    void testUpdateContainer_notExists() {
        ContainerSaveReqVO updateReqVO = buildReqVO("CTN-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> containerService.updateContainer(updateReqVO), CONTAINER_NOT_EXISTS);
    }

    @Test
    void testDeleteContainer_success() {
        Long id = containerService.createContainer(buildReqVO("CTN-A"));

        containerService.deleteContainer(id);

        assertNull(containerMapper.selectById(id));
    }

    @Test
    void testDeleteContainer_notExists() {
        assertServiceException(() -> containerService.deleteContainer(randomLongId()), CONTAINER_NOT_EXISTS);
    }

}
