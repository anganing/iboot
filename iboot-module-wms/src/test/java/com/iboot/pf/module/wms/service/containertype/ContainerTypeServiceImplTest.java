package com.iboot.pf.module.wms.service.containertype;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.containertype.ContainerTypeDO;
import com.iboot.pf.module.wms.dal.mysql.containertype.ContainerTypeMapper;
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
 * {@link ContainerTypeServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ContainerTypeServiceImpl.class)
public class ContainerTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ContainerTypeServiceImpl containerTypeService;

    @Resource
    private ContainerTypeMapper containerTypeMapper;

    @Test
    public void testCreateContainerType_success() {
        // 准备参数
        ContainerTypeSaveReqVO createReqVO = randomPojo(ContainerTypeSaveReqVO.class).setId(null);

        // 调用
        Long containerTypeId = containerTypeService.createContainerType(createReqVO);
        // 断言
        assertNotNull(containerTypeId);
        // 校验记录的属性是否正确
        ContainerTypeDO containerType = containerTypeMapper.selectById(containerTypeId);
        assertPojoEquals(createReqVO, containerType, "id");
    }

    @Test
    public void testUpdateContainerType_success() {
        // mock 数据
        ContainerTypeDO dbContainerType = randomPojo(ContainerTypeDO.class);
        containerTypeMapper.insert(dbContainerType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ContainerTypeSaveReqVO updateReqVO = randomPojo(ContainerTypeSaveReqVO.class, o -> {
            o.setId(dbContainerType.getId()); // 设置更新的 ID
        });

        // 调用
        containerTypeService.updateContainerType(updateReqVO);
        // 校验是否更新正确
        ContainerTypeDO containerType = containerTypeMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, containerType);
    }

    @Test
    public void testUpdateContainerType_notExists() {
        // 准备参数
        ContainerTypeSaveReqVO updateReqVO = randomPojo(ContainerTypeSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> containerTypeService.updateContainerType(updateReqVO), CONTAINER_TYPE_NOT_EXISTS);
    }

    @Test
    public void testDeleteContainerType_success() {
        // mock 数据
        ContainerTypeDO dbContainerType = randomPojo(ContainerTypeDO.class);
        containerTypeMapper.insert(dbContainerType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbContainerType.getId();

        // 调用
        containerTypeService.deleteContainerType(id);
       // 校验数据不存在了
       assertNull(containerTypeMapper.selectById(id));
    }

    @Test
    public void testDeleteContainerType_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> containerTypeService.deleteContainerType(id), CONTAINER_TYPE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetContainerTypePage() {
       // mock 数据
       ContainerTypeDO dbContainerType = randomPojo(ContainerTypeDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setStdLength(null);
           o.setStdWidth(null);
           o.setStdHeight(null);
           o.setMaxWeight(null);
           o.setMaxVolume(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       containerTypeMapper.insert(dbContainerType);
       // 测试 code 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setCode(null)));
       // 测试 name 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setName(null)));
       // 测试 stdLength 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setStdLength(null)));
       // 测试 stdWidth 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setStdWidth(null)));
       // 测试 stdHeight 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setStdHeight(null)));
       // 测试 maxWeight 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setMaxWeight(null)));
       // 测试 maxVolume 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setMaxVolume(null)));
       // 测试 status 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       containerTypeMapper.insert(cloneIgnoreId(dbContainerType, o -> o.setCreateTime(null)));
       // 准备参数
       ContainerTypePageReqVO reqVO = new ContainerTypePageReqVO();
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setStdLength(null);
       reqVO.setStdWidth(null);
       reqVO.setStdHeight(null);
       reqVO.setMaxWeight(null);
       reqVO.setMaxVolume(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ContainerTypeRespVO> pageResult = containerTypeService.getContainerTypePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbContainerType, pageResult.getList().get(0));
    }

}