package com.iboot.pf.module.mes.service.qcrule;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.*;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
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
 * {@link QcRuleServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(QcRuleServiceImpl.class)
public class QcRuleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcRuleServiceImpl qcRuleService;

    @Resource
    private QcRuleMapper qcRuleMapper;

    @Test
    public void testCreateQcRule_success() {
        // 准备参数
        QcRuleSaveReqVO createReqVO = randomPojo(QcRuleSaveReqVO.class).setId(null);

        // 调用
        Long qcRuleId = qcRuleService.createQcRule(createReqVO);
        // 断言
        assertNotNull(qcRuleId);
        // 校验记录的属性是否正确
        QcRuleDO qcRule = qcRuleMapper.selectById(qcRuleId);
        assertPojoEquals(createReqVO, qcRule, "id");
    }

    @Test
    public void testUpdateQcRule_success() {
        // mock 数据
        QcRuleDO dbQcRule = randomPojo(QcRuleDO.class);
        qcRuleMapper.insert(dbQcRule);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QcRuleSaveReqVO updateReqVO = randomPojo(QcRuleSaveReqVO.class, o -> {
            o.setId(dbQcRule.getId()); // 设置更新的 ID
        });

        // 调用
        qcRuleService.updateQcRule(updateReqVO);
        // 校验是否更新正确
        QcRuleDO qcRule = qcRuleMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, qcRule);
    }

    @Test
    public void testUpdateQcRule_notExists() {
        // 准备参数
        QcRuleSaveReqVO updateReqVO = randomPojo(QcRuleSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> qcRuleService.updateQcRule(updateReqVO), QC_RULE_NOT_EXISTS);
    }

    @Test
    public void testDeleteQcRule_success() {
        // mock 数据
        QcRuleDO dbQcRule = randomPojo(QcRuleDO.class);
        qcRuleMapper.insert(dbQcRule);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQcRule.getId();

        // 调用
        qcRuleService.deleteQcRule(id);
       // 校验数据不存在了
       assertNull(qcRuleMapper.selectById(id));
    }

    @Test
    public void testDeleteQcRule_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> qcRuleService.deleteQcRule(id), QC_RULE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQcRulePage() {
       // mock 数据
       QcRuleDO dbQcRule = randomPojo(QcRuleDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setTriggerPoint(null);
           o.setObjectType(null);
           o.setMatId(null);
           o.setMatCatId(null);
           o.setOpId(null);
           o.setWcId(null);
           o.setSupplierId(null);
           o.setQcPlanId(null);
           o.setQcMode(null);
           o.setPriority(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       qcRuleMapper.insert(dbQcRule);
       // 测试 code 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setCode(null)));
       // 测试 name 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setName(null)));
       // 测试 triggerPoint 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setTriggerPoint(null)));
       // 测试 objectType 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setObjectType(null)));
       // 测试 matId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setMatId(null)));
       // 测试 matCatId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setMatCatId(null)));
       // 测试 opId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setOpId(null)));
       // 测试 wcId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setWcId(null)));
       // 测试 supplierId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setSupplierId(null)));
       // 测试 qcPlanId 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setQcPlanId(null)));
       // 测试 qcMode 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setQcMode(null)));
       // 测试 priority 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setPriority(null)));
       // 测试 status 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       qcRuleMapper.insert(cloneIgnoreId(dbQcRule, o -> o.setCreateTime(null)));
       // 准备参数
       QcRulePageReqVO reqVO = new QcRulePageReqVO();
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setTriggerPoint(null);
       reqVO.setObjectType(null);
       reqVO.setMatId(null);
       reqVO.setMatCatId(null);
       reqVO.setOpId(null);
       reqVO.setWcId(null);
       reqVO.setSupplierId(null);
       reqVO.setQcPlanId(null);
       reqVO.setQcMode(null);
       reqVO.setPriority(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QcRuleRespVO> pageResult = qcRuleService.getQcRulePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQcRule, pageResult.getList().get(0));
    }

}