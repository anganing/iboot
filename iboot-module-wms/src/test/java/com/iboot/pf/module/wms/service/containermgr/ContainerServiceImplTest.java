package com.iboot.pf.module.wms.service.containermgr;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.containermgr.ContainerDO;
import com.iboot.pf.module.wms.dal.mysql.containermgr.ContainerMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ContainerServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ContainerServiceImpl.class)
public class ContainerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ContainerServiceImpl containerService;

    @Resource
    private ContainerMapper containerMapper;

    @Test
    public void testCreateContainer_success() {
        // 准备参数
        ContainerSaveReqVO createReqVO = randomPojo(ContainerSaveReqVO.class).setId(null);

        // 调用
        Long containerId = containerService.createContainer(createReqVO);
        // 断言
        assertNotNull(containerId);
        // 校验记录的属性是否正确
        ContainerDO container = containerMapper.selectById(containerId);
        assertPojoEquals(createReqVO, container, "id");
    }

    @Test
    public void testUpdateContainer_success() {
        // mock 数据
        ContainerDO dbContainer = randomPojo(ContainerDO.class);
        containerMapper.insert(dbContainer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ContainerSaveReqVO updateReqVO = randomPojo(ContainerSaveReqVO.class, o -> {
            o.setId(dbContainer.getId()); // 设置更新的 ID
        });

        // 调用
        containerService.updateContainer(updateReqVO);
        // 校验是否更新正确
        ContainerDO container = containerMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, container);
    }

    @Test
    public void testUpdateContainer_notExists() {
        // 准备参数
        ContainerSaveReqVO updateReqVO = randomPojo(ContainerSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> containerService.updateContainer(updateReqVO), CONTAINER_NOT_EXISTS);
    }

    @Test
    public void testDeleteContainer_success() {
        // mock 数据
        ContainerDO dbContainer = randomPojo(ContainerDO.class);
        containerMapper.insert(dbContainer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbContainer.getId();

        // 调用
        containerService.deleteContainer(id);
       // 校验数据不存在了
       assertNull(containerMapper.selectById(id));
    }

    @Test
    public void testDeleteContainer_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> containerService.deleteContainer(id), CONTAINER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetContainerPage() {
       // mock 数据
       ContainerDO dbContainer = randomPojo(ContainerDO.class, o -> { // 等会查询到
           o.setContainerNo(null);
           o.setTypeId(null);
           o.setInvOrgId(null);
           o.setWhId(null);
           o.setLocId(null);
           o.setLength(null);
           o.setWidth(null);
           o.setHeight(null);
           o.setGrossWeight(null);
           o.setStatus(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       containerMapper.insert(dbContainer);
       // 测试 containerNo 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setContainerNo(null)));
       // 测试 typeId 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setTypeId(null)));
       // 测试 invOrgId 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setInvOrgId(null)));
       // 测试 whId 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setWhId(null)));
       // 测试 locId 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setLocId(null)));
       // 测试 length 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setLength(null)));
       // 测试 width 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setWidth(null)));
       // 测试 height 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setHeight(null)));
       // 测试 grossWeight 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setGrossWeight(null)));
       // 测试 status 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setStatus(null)));
       // 测试 version 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       containerMapper.insert(cloneIgnoreId(dbContainer, o -> o.setCreateTime(null)));
       // 准备参数
       ContainerPageReqVO reqVO = new ContainerPageReqVO();
       reqVO.setContainerNo(null);
       reqVO.setTypeId(null);
       reqVO.setInvOrgId(null);
       reqVO.setWhId(null);
       reqVO.setLocId(null);
       reqVO.setLength(null);
       reqVO.setWidth(null);
       reqVO.setHeight(null);
       reqVO.setGrossWeight(null);
       reqVO.setStatus(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ContainerRespVO> pageResult = containerService.getContainerPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbContainer, pageResult.getList().get(0));
    }

}