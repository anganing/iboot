package com.iboot.pf.module.mes.service.defect;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.*;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
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
 * {@link DefectServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(DefectServiceImpl.class)
public class DefectServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DefectServiceImpl defectService;

    @Resource
    private DefectMapper defectMapper;

    @Test
    public void testCreateDefect_success() {
        // 准备参数
        DefectSaveReqVO createReqVO = randomPojo(DefectSaveReqVO.class).setId(null);

        // 调用
        Long defectId = defectService.createDefect(createReqVO);
        // 断言
        assertNotNull(defectId);
        // 校验记录的属性是否正确
        DefectDO defect = defectMapper.selectById(defectId);
        assertPojoEquals(createReqVO, defect, "id");
    }

    @Test
    public void testUpdateDefect_success() {
        // mock 数据
        DefectDO dbDefect = randomPojo(DefectDO.class);
        defectMapper.insert(dbDefect);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DefectSaveReqVO updateReqVO = randomPojo(DefectSaveReqVO.class, o -> {
            o.setId(dbDefect.getId()); // 设置更新的 ID
        });

        // 调用
        defectService.updateDefect(updateReqVO);
        // 校验是否更新正确
        DefectDO defect = defectMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, defect);
    }

    @Test
    public void testUpdateDefect_notExists() {
        // 准备参数
        DefectSaveReqVO updateReqVO = randomPojo(DefectSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> defectService.updateDefect(updateReqVO), DEFECT_NOT_EXISTS);
    }

    @Test
    public void testDeleteDefect_success() {
        // mock 数据
        DefectDO dbDefect = randomPojo(DefectDO.class);
        defectMapper.insert(dbDefect);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDefect.getId();

        // 调用
        defectService.deleteDefect(id);
       // 校验数据不存在了
       assertNull(defectMapper.selectById(id));
    }

    @Test
    public void testDeleteDefect_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> defectService.deleteDefect(id), DEFECT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDefectPage() {
       // mock 数据
       DefectDO dbDefect = randomPojo(DefectDO.class, o -> { // 等会查询到
           o.setTaskId(null);
           o.setResultId(null);
           o.setDefectCode(null);
           o.setDefectName(null);
           o.setDefectLevel(null);
           o.setQty(null);
           o.setDescription(null);
           o.setLocation(null);
           o.setAttachmentUrls(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       defectMapper.insert(dbDefect);
       // 测试 taskId 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setTaskId(null)));
       // 测试 resultId 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setResultId(null)));
       // 测试 defectCode 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setDefectCode(null)));
       // 测试 defectName 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setDefectName(null)));
       // 测试 defectLevel 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setDefectLevel(null)));
       // 测试 qty 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setQty(null)));
       // 测试 description 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setDescription(null)));
       // 测试 location 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setLocation(null)));
       // 测试 attachmentUrls 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setAttachmentUrls(null)));
       // 测试 remark 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       defectMapper.insert(cloneIgnoreId(dbDefect, o -> o.setCreateTime(null)));
       // 准备参数
       DefectPageReqVO reqVO = new DefectPageReqVO();
       reqVO.setTaskId(null);
       reqVO.setResultId(null);
       reqVO.setDefectCode(null);
       reqVO.setDefectName(null);
       reqVO.setDefectLevel(null);
       reqVO.setQty(null);
       reqVO.setDescription(null);
       reqVO.setLocation(null);
       reqVO.setAttachmentUrls(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<DefectRespVO> pageResult = defectService.getDefectPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDefect, pageResult.getList().get(0));
    }

}