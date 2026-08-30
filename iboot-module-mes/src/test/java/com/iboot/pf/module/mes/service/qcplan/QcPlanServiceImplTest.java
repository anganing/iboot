package com.iboot.pf.module.mes.service.qcplan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.*;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
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
 * {@link QcPlanServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(QcPlanServiceImpl.class)
public class QcPlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcPlanServiceImpl qcPlanService;

    @Resource
    private QcPlanMapper qcPlanMapper;

    @Test
    public void testCreateQcPlan_success() {
        // 准备参数
        QcPlanSaveReqVO createReqVO = randomPojo(QcPlanSaveReqVO.class).setId(null);

        // 调用
        Long qcPlanId = qcPlanService.createQcPlan(createReqVO);
        // 断言
        assertNotNull(qcPlanId);
        // 校验记录的属性是否正确
        QcPlanDO qcPlan = qcPlanMapper.selectById(qcPlanId);
        assertPojoEquals(createReqVO, qcPlan, "id");
    }

    @Test
    public void testUpdateQcPlan_success() {
        // mock 数据
        QcPlanDO dbQcPlan = randomPojo(QcPlanDO.class);
        qcPlanMapper.insert(dbQcPlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QcPlanSaveReqVO updateReqVO = randomPojo(QcPlanSaveReqVO.class, o -> {
            o.setId(dbQcPlan.getId()); // 设置更新的 ID
        });

        // 调用
        qcPlanService.updateQcPlan(updateReqVO);
        // 校验是否更新正确
        QcPlanDO qcPlan = qcPlanMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, qcPlan);
    }

    @Test
    public void testUpdateQcPlan_notExists() {
        // 准备参数
        QcPlanSaveReqVO updateReqVO = randomPojo(QcPlanSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> qcPlanService.updateQcPlan(updateReqVO), QC_PLAN_NOT_EXISTS);
    }

    @Test
    public void testDeleteQcPlan_success() {
        // mock 数据
        QcPlanDO dbQcPlan = randomPojo(QcPlanDO.class);
        qcPlanMapper.insert(dbQcPlan);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQcPlan.getId();

        // 调用
        qcPlanService.deleteQcPlan(id);
       // 校验数据不存在了
       assertNull(qcPlanMapper.selectById(id));
    }

    @Test
    public void testDeleteQcPlan_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> qcPlanService.deleteQcPlan(id), QC_PLAN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQcPlanPage() {
       // mock 数据
       QcPlanDO dbQcPlan = randomPojo(QcPlanDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setVersionNo(null);
           o.setSampleType(null);
           o.setSampleValue(null);
           o.setAql(null);
           o.setAcceptanceRule(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       qcPlanMapper.insert(dbQcPlan);
       // 测试 code 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setCode(null)));
       // 测试 name 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setName(null)));
       // 测试 versionNo 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setVersionNo(null)));
       // 测试 sampleType 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setSampleType(null)));
       // 测试 sampleValue 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setSampleValue(null)));
       // 测试 aql 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setAql(null)));
       // 测试 acceptanceRule 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setAcceptanceRule(null)));
       // 测试 status 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       qcPlanMapper.insert(cloneIgnoreId(dbQcPlan, o -> o.setCreateTime(null)));
       // 准备参数
       QcPlanPageReqVO reqVO = new QcPlanPageReqVO();
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setVersionNo(null);
       reqVO.setSampleType(null);
       reqVO.setSampleValue(null);
       reqVO.setAql(null);
       reqVO.setAcceptanceRule(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QcPlanRespVO> pageResult = qcPlanService.getQcPlanPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQcPlan, pageResult.getList().get(0));
    }

}