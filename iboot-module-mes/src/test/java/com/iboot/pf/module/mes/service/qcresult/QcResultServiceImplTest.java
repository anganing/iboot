package com.iboot.pf.module.mes.service.qcresult;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.*;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
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
 * {@link QcResultServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(QcResultServiceImpl.class)
public class QcResultServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcResultServiceImpl qcResultService;

    @Resource
    private QcResultMapper qcResultMapper;

    @Test
    public void testCreateQcResult_success() {
        // 准备参数
        QcResultSaveReqVO createReqVO = randomPojo(QcResultSaveReqVO.class).setId(null);

        // 调用
        Long qcResultId = qcResultService.createQcResult(createReqVO);
        // 断言
        assertNotNull(qcResultId);
        // 校验记录的属性是否正确
        QcResultDO qcResult = qcResultMapper.selectById(qcResultId);
        assertPojoEquals(createReqVO, qcResult, "id");
    }

    @Test
    public void testUpdateQcResult_success() {
        // mock 数据
        QcResultDO dbQcResult = randomPojo(QcResultDO.class);
        qcResultMapper.insert(dbQcResult);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QcResultSaveReqVO updateReqVO = randomPojo(QcResultSaveReqVO.class, o -> {
            o.setId(dbQcResult.getId()); // 设置更新的 ID
        });

        // 调用
        qcResultService.updateQcResult(updateReqVO);
        // 校验是否更新正确
        QcResultDO qcResult = qcResultMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, qcResult);
    }

    @Test
    public void testUpdateQcResult_notExists() {
        // 准备参数
        QcResultSaveReqVO updateReqVO = randomPojo(QcResultSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> qcResultService.updateQcResult(updateReqVO), QC_RESULT_NOT_EXISTS);
    }

    @Test
    public void testDeleteQcResult_success() {
        // mock 数据
        QcResultDO dbQcResult = randomPojo(QcResultDO.class);
        qcResultMapper.insert(dbQcResult);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQcResult.getId();

        // 调用
        qcResultService.deleteQcResult(id);
       // 校验数据不存在了
       assertNull(qcResultMapper.selectById(id));
    }

    @Test
    public void testDeleteQcResult_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> qcResultService.deleteQcResult(id), QC_RESULT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQcResultPage() {
       // mock 数据
       QcResultDO dbQcResult = randomPojo(QcResultDO.class, o -> { // 等会查询到
           o.setTaskId(null);
           o.setItemId(null);
           o.setSampleNo(null);
           o.setItemCode(null);
           o.setItemName(null);
           o.setActualValue(null);
           o.setPassFlag(null);
           o.setInspectorId(null);
           o.setInspectTime(null);
           o.setQtyQualified(null);
           o.setQtyRejected(null);
           o.setConclusion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       qcResultMapper.insert(dbQcResult);
       // 测试 taskId 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setTaskId(null)));
       // 测试 itemId 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setItemId(null)));
       // 测试 sampleNo 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setSampleNo(null)));
       // 测试 itemCode 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setItemCode(null)));
       // 测试 itemName 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setItemName(null)));
       // 测试 actualValue 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setActualValue(null)));
       // 测试 passFlag 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setPassFlag(null)));
       // 测试 inspectorId 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setInspectorId(null)));
       // 测试 inspectTime 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setInspectTime(null)));
       // 测试 qtyQualified 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setQtyQualified(null)));
       // 测试 qtyRejected 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setQtyRejected(null)));
       // 测试 conclusion 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setConclusion(null)));
       // 测试 remark 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       qcResultMapper.insert(cloneIgnoreId(dbQcResult, o -> o.setCreateTime(null)));
       // 准备参数
       QcResultPageReqVO reqVO = new QcResultPageReqVO();
       reqVO.setTaskId(null);
       reqVO.setItemId(null);
       reqVO.setSampleNo(null);
       reqVO.setItemCode(null);
       reqVO.setItemName(null);
       reqVO.setActualValue(null);
       reqVO.setPassFlag(null);
       reqVO.setInspectorId(null);
       reqVO.setInspectTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setQtyQualified(null);
       reqVO.setQtyRejected(null);
       reqVO.setConclusion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QcResultRespVO> pageResult = qcResultService.getQcResultPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQcResult, pageResult.getList().get(0));
    }

}