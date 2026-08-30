package com.iboot.pf.module.wms.service.wave;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.wave.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.wave.WaveDO;
import com.iboot.pf.module.wms.dal.mysql.wave.WaveMapper;
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
 * {@link WaveServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(WaveServiceImpl.class)
public class WaveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WaveServiceImpl waveService;

    @Resource
    private WaveMapper waveMapper;

    @Test
    public void testCreateWave_success() {
        // 准备参数
        WaveSaveReqVO createReqVO = randomPojo(WaveSaveReqVO.class).setId(null);

        // 调用
        Long waveId = waveService.createWave(createReqVO);
        // 断言
        assertNotNull(waveId);
        // 校验记录的属性是否正确
        WaveDO wave = waveMapper.selectById(waveId);
        assertPojoEquals(createReqVO, wave, "id");
    }

    @Test
    public void testUpdateWave_success() {
        // mock 数据
        WaveDO dbWave = randomPojo(WaveDO.class);
        waveMapper.insert(dbWave);// @Sql: 先插入出一条存在的数据
        // 准备参数
        WaveSaveReqVO updateReqVO = randomPojo(WaveSaveReqVO.class, o -> {
            o.setId(dbWave.getId()); // 设置更新的 ID
        });

        // 调用
        waveService.updateWave(updateReqVO);
        // 校验是否更新正确
        WaveDO wave = waveMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, wave);
    }

    @Test
    public void testUpdateWave_notExists() {
        // 准备参数
        WaveSaveReqVO updateReqVO = randomPojo(WaveSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> waveService.updateWave(updateReqVO), WAVE_NOT_EXISTS);
    }

    @Test
    public void testDeleteWave_success() {
        // mock 数据
        WaveDO dbWave = randomPojo(WaveDO.class);
        waveMapper.insert(dbWave);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbWave.getId();

        // 调用
        waveService.deleteWave(id);
       // 校验数据不存在了
       assertNull(waveMapper.selectById(id));
    }

    @Test
    public void testDeleteWave_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> waveService.deleteWave(id), WAVE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetWavePage() {
       // mock 数据
       WaveDO dbWave = randomPojo(WaveDO.class, o -> { // 等会查询到
           o.setWaveNo(null);
           o.setWhId(null);
           o.setWaveType(null);
           o.setPlanStartTime(null);
           o.setPlanEndTime(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       waveMapper.insert(dbWave);
       // 测试 waveNo 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setWaveNo(null)));
       // 测试 whId 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setWhId(null)));
       // 测试 waveType 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setWaveType(null)));
       // 测试 planStartTime 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setPlanStartTime(null)));
       // 测试 planEndTime 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setPlanEndTime(null)));
       // 测试 status 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       waveMapper.insert(cloneIgnoreId(dbWave, o -> o.setCreateTime(null)));
       // 准备参数
       WavePageReqVO reqVO = new WavePageReqVO();
       reqVO.setWaveNo(null);
       reqVO.setWhId(null);
       reqVO.setWaveType(null);
       reqVO.setPlanStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPlanEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<WaveRespVO> pageResult = waveService.getWavePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbWave, pageResult.getList().get(0));
    }

}