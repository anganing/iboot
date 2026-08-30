package com.iboot.pf.module.mes.service.materialreq;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.*;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MaterialReqServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(MaterialReqServiceImpl.class)
public class MaterialReqServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MaterialReqServiceImpl materialReqService;

    @Resource
    private MaterialReqMapper materialReqMapper;

    @Test
    public void testCreateMaterialReq_success() {
        // 准备参数
        MaterialReqSaveReqVO createReqVO = randomPojo(MaterialReqSaveReqVO.class).setId(null);

        // 调用
        Long materialReqId = materialReqService.createMaterialReq(createReqVO);
        // 断言
        assertNotNull(materialReqId);
        // 校验记录的属性是否正确
        MaterialReqDO materialReq = materialReqMapper.selectById(materialReqId);
        assertPojoEquals(createReqVO, materialReq, "id");
    }

    @Test
    public void testUpdateMaterialReq_success() {
        // mock 数据
        MaterialReqDO dbMaterialReq = randomPojo(MaterialReqDO.class);
        materialReqMapper.insert(dbMaterialReq);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MaterialReqSaveReqVO updateReqVO = randomPojo(MaterialReqSaveReqVO.class, o -> {
            o.setId(dbMaterialReq.getId()); // 设置更新的 ID
        });

        // 调用
        materialReqService.updateMaterialReq(updateReqVO);
        // 校验是否更新正确
        MaterialReqDO materialReq = materialReqMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, materialReq);
    }

    @Test
    public void testUpdateMaterialReq_notExists() {
        // 准备参数
        MaterialReqSaveReqVO updateReqVO = randomPojo(MaterialReqSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> materialReqService.updateMaterialReq(updateReqVO), MATERIAL_REQ_NOT_EXISTS);
    }

    @Test
    public void testDeleteMaterialReq_success() {
        // mock 数据
        MaterialReqDO dbMaterialReq = randomPojo(MaterialReqDO.class);
        materialReqMapper.insert(dbMaterialReq);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMaterialReq.getId();

        // 调用
        materialReqService.deleteMaterialReq(id);
       // 校验数据不存在了
       assertNull(materialReqMapper.selectById(id));
    }

    @Test
    public void testDeleteMaterialReq_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> materialReqService.deleteMaterialReq(id), MATERIAL_REQ_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMaterialReqPage() {
       // mock 数据
       MaterialReqDO dbMaterialReq = randomPojo(MaterialReqDO.class, o -> { // 等会查询到
           o.setReqNo(null);
           o.setProdOrderId(null);
           o.setProdOrderNo(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setReqType(null);
           o.setInvOrgId(null);
           o.setWhId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setWmsBizNo(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       materialReqMapper.insert(dbMaterialReq);
       // 测试 reqNo 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setReqNo(null)));
       // 测试 prodOrderId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setProdOrderId(null)));
       // 测试 prodOrderNo 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setProdOrderNo(null)));
       // 测试 workOrderId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setOpTaskId(null)));
       // 测试 reqType 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setReqType(null)));
       // 测试 invOrgId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setInvOrgId(null)));
       // 测试 whId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setWhId(null)));
       // 测试 projectId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setProjectTaskId(null)));
       // 测试 wmsBizNo 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setWmsBizNo(null)));
       // 测试 status 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       materialReqMapper.insert(cloneIgnoreId(dbMaterialReq, o -> o.setCreateTime(null)));
       // 准备参数
       MaterialReqPageReqVO reqVO = new MaterialReqPageReqVO();
       reqVO.setReqNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setProdOrderNo(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setReqType(null);
       reqVO.setInvOrgId(null);
       reqVO.setWhId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setWmsBizNo(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<MaterialReqRespVO> pageResult = materialReqService.getMaterialReqPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbMaterialReq, pageResult.getList().get(0));
    }

}