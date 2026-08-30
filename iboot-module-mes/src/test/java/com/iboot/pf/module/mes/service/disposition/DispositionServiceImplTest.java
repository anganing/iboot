package com.iboot.pf.module.mes.service.disposition;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.*;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
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
 * {@link DispositionServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(DispositionServiceImpl.class)
public class DispositionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DispositionServiceImpl dispositionService;

    @Resource
    private DispositionMapper dispositionMapper;

    @Test
    public void testCreateDisposition_success() {
        // 准备参数
        DispositionSaveReqVO createReqVO = randomPojo(DispositionSaveReqVO.class).setId(null);

        // 调用
        Long dispositionId = dispositionService.createDisposition(createReqVO);
        // 断言
        assertNotNull(dispositionId);
        // 校验记录的属性是否正确
        DispositionDO disposition = dispositionMapper.selectById(dispositionId);
        assertPojoEquals(createReqVO, disposition, "id");
    }

    @Test
    public void testUpdateDisposition_success() {
        // mock 数据
        DispositionDO dbDisposition = randomPojo(DispositionDO.class);
        dispositionMapper.insert(dbDisposition);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DispositionSaveReqVO updateReqVO = randomPojo(DispositionSaveReqVO.class, o -> {
            o.setId(dbDisposition.getId()); // 设置更新的 ID
        });

        // 调用
        dispositionService.updateDisposition(updateReqVO);
        // 校验是否更新正确
        DispositionDO disposition = dispositionMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, disposition);
    }

    @Test
    public void testUpdateDisposition_notExists() {
        // 准备参数
        DispositionSaveReqVO updateReqVO = randomPojo(DispositionSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dispositionService.updateDisposition(updateReqVO), DISPOSITION_NOT_EXISTS);
    }

    @Test
    public void testDeleteDisposition_success() {
        // mock 数据
        DispositionDO dbDisposition = randomPojo(DispositionDO.class);
        dispositionMapper.insert(dbDisposition);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDisposition.getId();

        // 调用
        dispositionService.deleteDisposition(id);
       // 校验数据不存在了
       assertNull(dispositionMapper.selectById(id));
    }

    @Test
    public void testDeleteDisposition_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> dispositionService.deleteDisposition(id), DISPOSITION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDispositionPage() {
       // mock 数据
       DispositionDO dbDisposition = randomPojo(DispositionDO.class, o -> { // 等会查询到
           o.setDispositionNo(null);
           o.setTaskId(null);
           o.setDispositionType(null);
           o.setQty(null);
           o.setTargetBizType(null);
           o.setTargetBizNo(null);
           o.setApprovedBy(null);
           o.setApproveTime(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       dispositionMapper.insert(dbDisposition);
       // 测试 dispositionNo 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setDispositionNo(null)));
       // 测试 taskId 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setTaskId(null)));
       // 测试 dispositionType 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setDispositionType(null)));
       // 测试 qty 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setQty(null)));
       // 测试 targetBizType 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setTargetBizType(null)));
       // 测试 targetBizNo 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setTargetBizNo(null)));
       // 测试 approvedBy 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setApprovedBy(null)));
       // 测试 approveTime 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setApproveTime(null)));
       // 测试 status 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       dispositionMapper.insert(cloneIgnoreId(dbDisposition, o -> o.setCreateTime(null)));
       // 准备参数
       DispositionPageReqVO reqVO = new DispositionPageReqVO();
       reqVO.setDispositionNo(null);
       reqVO.setTaskId(null);
       reqVO.setDispositionType(null);
       reqVO.setQty(null);
       reqVO.setTargetBizType(null);
       reqVO.setTargetBizNo(null);
       reqVO.setApprovedBy(null);
       reqVO.setApproveTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<DispositionRespVO> pageResult = dispositionService.getDispositionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDisposition, pageResult.getList().get(0));
    }

}