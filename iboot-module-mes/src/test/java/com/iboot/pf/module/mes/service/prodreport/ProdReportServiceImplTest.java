package com.iboot.pf.module.mes.service.prodreport;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.*;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
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
 * {@link ProdReportServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProdReportServiceImpl.class)
public class ProdReportServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdReportServiceImpl prodReportService;

    @Resource
    private ProdReportMapper prodReportMapper;

    @Test
    public void testCreateProdReport_success() {
        // 准备参数
        ProdReportSaveReqVO createReqVO = randomPojo(ProdReportSaveReqVO.class).setId(null);

        // 调用
        Long prodReportId = prodReportService.createProdReport(createReqVO);
        // 断言
        assertNotNull(prodReportId);
        // 校验记录的属性是否正确
        ProdReportDO prodReport = prodReportMapper.selectById(prodReportId);
        assertPojoEquals(createReqVO, prodReport, "id");
    }

    @Test
    public void testUpdateProdReport_success() {
        // mock 数据
        ProdReportDO dbProdReport = randomPojo(ProdReportDO.class);
        prodReportMapper.insert(dbProdReport);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProdReportSaveReqVO updateReqVO = randomPojo(ProdReportSaveReqVO.class, o -> {
            o.setId(dbProdReport.getId()); // 设置更新的 ID
        });

        // 调用
        prodReportService.updateProdReport(updateReqVO);
        // 校验是否更新正确
        ProdReportDO prodReport = prodReportMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, prodReport);
    }

    @Test
    public void testUpdateProdReport_notExists() {
        // 准备参数
        ProdReportSaveReqVO updateReqVO = randomPojo(ProdReportSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prodReportService.updateProdReport(updateReqVO), PROD_REPORT_NOT_EXISTS);
    }

    @Test
    public void testDeleteProdReport_success() {
        // mock 数据
        ProdReportDO dbProdReport = randomPojo(ProdReportDO.class);
        prodReportMapper.insert(dbProdReport);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProdReport.getId();

        // 调用
        prodReportService.deleteProdReport(id);
       // 校验数据不存在了
       assertNull(prodReportMapper.selectById(id));
    }

    @Test
    public void testDeleteProdReport_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prodReportService.deleteProdReport(id), PROD_REPORT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProdReportPage() {
       // mock 数据
       ProdReportDO dbProdReport = randomPojo(ProdReportDO.class, o -> { // 等会查询到
           o.setReportNo(null);
           o.setProdOrderId(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setWorkerId(null);
           o.setResId(null);
           o.setShiftId(null);
           o.setQtyGood(null);
           o.setQtyScrap(null);
           o.setQtyRework(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setReportTime(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       prodReportMapper.insert(dbProdReport);
       // 测试 reportNo 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setReportNo(null)));
       // 测试 prodOrderId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setProdOrderId(null)));
       // 测试 workOrderId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setOpTaskId(null)));
       // 测试 workerId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setWorkerId(null)));
       // 测试 resId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setResId(null)));
       // 测试 shiftId 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setShiftId(null)));
       // 测试 qtyGood 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setQtyGood(null)));
       // 测试 qtyScrap 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setQtyScrap(null)));
       // 测试 qtyRework 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setQtyRework(null)));
       // 测试 startTime 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setEndTime(null)));
       // 测试 reportTime 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setReportTime(null)));
       // 测试 status 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       prodReportMapper.insert(cloneIgnoreId(dbProdReport, o -> o.setCreateTime(null)));
       // 准备参数
       ProdReportPageReqVO reqVO = new ProdReportPageReqVO();
       reqVO.setReportNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setWorkerId(null);
       reqVO.setResId(null);
       reqVO.setShiftId(null);
       reqVO.setQtyGood(null);
       reqVO.setQtyScrap(null);
       reqVO.setQtyRework(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setReportTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProdReportRespVO> pageResult = prodReportService.getProdReportPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProdReport, pageResult.getList().get(0));
    }

}