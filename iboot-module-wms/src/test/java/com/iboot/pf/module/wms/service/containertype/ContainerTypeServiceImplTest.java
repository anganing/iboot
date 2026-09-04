package com.iboot.pf.module.wms.service.containertype;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.containertype.vo.ContainerTypeSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.container.WmsContainerTypeDO;
import com.iboot.pf.module.wms.dal.mysql.containertype.ContainerTypeMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_TYPE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.CONTAINER_TYPE_CODE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 容器类型 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(ContainerTypeServiceImpl.class)
class ContainerTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ContainerTypeService containerTypeService;
    @Resource
    private ContainerTypeMapper containerTypeMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "CT0001" : invocation.getArgument(1));
    }

    private ContainerTypeSaveReqVO buildReqVO(String code) {
        ContainerTypeSaveReqVO reqVO = new ContainerTypeSaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("标准托盘");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateContainerType_blankCodeFilledByRule() {
        Long id = containerTypeService.createContainerType(buildReqVO(null));

        WmsContainerTypeDO entity = containerTypeMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("CT0001", entity.getCode());
    }

    @Test
    void testCreateContainerType_duplicateRejected() {
        containerTypeService.createContainerType(buildReqVO("CT0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> containerTypeService.createContainerType(buildReqVO("CT0-A")), CONTAINER_TYPE_CODE_DUPLICATE);
    }

    @Test
    void testUpdateContainerType_keepOwnCode() {
        Long id = containerTypeService.createContainerType(buildReqVO("CT0-A"));

        // 改自己时业务编码没变，不应该撞到自己
        ContainerTypeSaveReqVO updateReqVO = buildReqVO("CT0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        containerTypeService.updateContainerType(updateReqVO);

        assertEquals("规则调整", containerTypeMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateContainerType_duplicateRejected() {
        containerTypeService.createContainerType(buildReqVO("CT0-A"));
        Long secondId = containerTypeService.createContainerType(buildReqVO("CT0-B"));

        ContainerTypeSaveReqVO updateReqVO = buildReqVO("CT0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> containerTypeService.updateContainerType(updateReqVO), CONTAINER_TYPE_CODE_DUPLICATE);
    }

    @Test
    void testUpdateContainerType_notExists() {
        ContainerTypeSaveReqVO updateReqVO = buildReqVO("CT0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> containerTypeService.updateContainerType(updateReqVO), CONTAINER_TYPE_NOT_EXISTS);
    }

    @Test
    void testDeleteContainerType_success() {
        Long id = containerTypeService.createContainerType(buildReqVO("CT0-A"));

        containerTypeService.deleteContainerType(id);

        assertNull(containerTypeMapper.selectById(id));
    }

    @Test
    void testDeleteContainerType_notExists() {
        assertServiceException(() -> containerTypeService.deleteContainerType(randomLongId()), CONTAINER_TYPE_NOT_EXISTS);
    }

}
