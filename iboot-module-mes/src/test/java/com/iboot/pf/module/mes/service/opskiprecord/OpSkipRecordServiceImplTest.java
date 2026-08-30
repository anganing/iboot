package com.iboot.pf.module.mes.service.opskiprecord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.*;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
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
 * {@link OpSkipRecordServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(OpSkipRecordServiceImpl.class)
public class OpSkipRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OpSkipRecordServiceImpl opSkipRecordService;

    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;

    @Test
    public void testCreateOpSkipRecord_success() {
        // 准备参数
        OpSkipRecordSaveReqVO createReqVO = randomPojo(OpSkipRecordSaveReqVO.class).setId(null);

        // 调用
        Long opSkipRecordId = opSkipRecordService.createOpSkipRecord(createReqVO);
        // 断言
        assertNotNull(opSkipRecordId);
        // 校验记录的属性是否正确
        OpSkipRecordDO opSkipRecord = opSkipRecordMapper.selectById(opSkipRecordId);
        assertPojoEquals(createReqVO, opSkipRecord, "id");
    }

    @Test
    public void testUpdateOpSkipRecord_success() {
        // mock 数据
        OpSkipRecordDO dbOpSkipRecord = randomPojo(OpSkipRecordDO.class);
        opSkipRecordMapper.insert(dbOpSkipRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OpSkipRecordSaveReqVO updateReqVO = randomPojo(OpSkipRecordSaveReqVO.class, o -> {
            o.setId(dbOpSkipRecord.getId()); // 设置更新的 ID
        });

        // 调用
        opSkipRecordService.updateOpSkipRecord(updateReqVO);
        // 校验是否更新正确
        OpSkipRecordDO opSkipRecord = opSkipRecordMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, opSkipRecord);
    }

    @Test
    public void testUpdateOpSkipRecord_notExists() {
        // 准备参数
        OpSkipRecordSaveReqVO updateReqVO = randomPojo(OpSkipRecordSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> opSkipRecordService.updateOpSkipRecord(updateReqVO), OP_SKIP_RECORD_NOT_EXISTS);
    }

    @Test
    public void testDeleteOpSkipRecord_success() {
        // mock 数据
        OpSkipRecordDO dbOpSkipRecord = randomPojo(OpSkipRecordDO.class);
        opSkipRecordMapper.insert(dbOpSkipRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOpSkipRecord.getId();

        // 调用
        opSkipRecordService.deleteOpSkipRecord(id);
       // 校验数据不存在了
       assertNull(opSkipRecordMapper.selectById(id));
    }

    @Test
    public void testDeleteOpSkipRecord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> opSkipRecordService.deleteOpSkipRecord(id), OP_SKIP_RECORD_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOpSkipRecordPage() {
       // mock 数据
       OpSkipRecordDO dbOpSkipRecord = randomPojo(OpSkipRecordDO.class, o -> { // 等会查询到
           o.setOpTaskId(null);
           o.setSkipReasonType(null);
           o.setSkipReason(null);
           o.setOperatorId(null);
           o.setApprovedBy(null);
           o.setApproveStatus(null);
           o.setSkipTime(null);
           o.setEffectRemark(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       opSkipRecordMapper.insert(dbOpSkipRecord);
       // 测试 opTaskId 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setOpTaskId(null)));
       // 测试 skipReasonType 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setSkipReasonType(null)));
       // 测试 skipReason 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setSkipReason(null)));
       // 测试 operatorId 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setOperatorId(null)));
       // 测试 approvedBy 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setApprovedBy(null)));
       // 测试 approveStatus 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setApproveStatus(null)));
       // 测试 skipTime 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setSkipTime(null)));
       // 测试 effectRemark 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setEffectRemark(null)));
       // 测试 status 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       opSkipRecordMapper.insert(cloneIgnoreId(dbOpSkipRecord, o -> o.setCreateTime(null)));
       // 准备参数
       OpSkipRecordPageReqVO reqVO = new OpSkipRecordPageReqVO();
       reqVO.setOpTaskId(null);
       reqVO.setSkipReasonType(null);
       reqVO.setSkipReason(null);
       reqVO.setOperatorId(null);
       reqVO.setApprovedBy(null);
       reqVO.setApproveStatus(null);
       reqVO.setSkipTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEffectRemark(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OpSkipRecordRespVO> pageResult = opSkipRecordService.getOpSkipRecordPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOpSkipRecord, pageResult.getList().get(0));
    }

}