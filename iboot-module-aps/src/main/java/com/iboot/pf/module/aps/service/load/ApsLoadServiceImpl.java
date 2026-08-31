package com.iboot.pf.module.aps.service.load;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadPageReqVO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadRespVO;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.task.ApsTaskDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.load.ApsLoadMapper;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsResourceProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.task.ApsTaskMapper;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.LOAD_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;

/**
 * APS-资源负荷 Service 实现类。
 *
 * <p>刷新逻辑：把版本内的排程任务按“资源 + 日期”累加占用分钟，再和资源日历投影的当日可用分钟对比，
 * 得出负荷率与超负荷分钟数。整表重建，所以先物理删除旧数据（逻辑删除会撞唯一索引）。
 *
 * @author iboot
 */
@Service
@Validated
public class ApsLoadServiceImpl implements ApsLoadService {

    @Resource
    private ApsLoadMapper loadMapper;
    @Resource
    private ApsTaskMapper taskMapper;
    @Resource
    private ApsResourceProjectionMapper resourceProjectionMapper;
    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsCommandService commandService;

    @Override
    public LoadRespVO getLoad(Long id) {
        LoadRespVO respVO = id == null ? null : loadMapper.selectJoinById(id);
        if (respVO == null) {
            throw exception(LOAD_NOT_EXISTS);
        }
        return respVO;
    }

    @Override
    public PageResult<LoadRespVO> getLoadPage(LoadPageReqVO pageReqVO) {
        return loadMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshLoad(RefreshLoadReqDTO reqDTO) {
        commandService.executeVoid(ApsCommandTypeEnum.REFRESH_LOAD, reqDTO, () -> {
            ApsVersionDO version = versionService.getVersionDO(reqDTO.getVersionId());
            if (version == null) {
                throw exception(VERSION_NOT_EXISTS);
            }
            Map<ResourceDayKey, Integer> capacityMap = loadDailyCapacity(version.getScenarioId());
            List<ApsLoadDO> rows = buildLoadRows(reqDTO.getVersionId(), capacityMap);
            // 整表重建：先物理删旧数据，唯一索引含 deleted，逻辑删除会让重算插不进去
            loadMapper.deletePhysicalByVersionId(reqDTO.getVersionId());
            if (CollUtil.isNotEmpty(rows)) {
                loadMapper.insertBatch(rows);
            }
        });
    }

    @Override
    public List<ApsLoadDO> getLoadDOListByVersionId(Long versionId) {
        return loadMapper.selectListByVersionId(versionId);
    }

    /**
     * 资源当日可用分钟数。
     *
     * <p>取班次为 0 的日汇总记录；同一资源多天各自一条，这里按“资源 + 日期”合成键累加，
     * 避免为每一天单独查一次投影。
     */
    private Map<ResourceDayKey, Integer> loadDailyCapacity(Long scenarioId) {
        Map<ResourceDayKey, Integer> capacityMap = new HashMap<>();
        for (ApsResourceProjectionDO projection : resourceProjectionMapper.selectDailyListByScenarioId(scenarioId)) {
            capacityMap.merge(new ResourceDayKey(projection.getSourceResId(), projection.getWorkDate()),
                    projection.getAvailableMinutes(), Integer::sum);
        }
        return capacityMap;
    }

    /** 按“资源 + 日期”汇总任务占用，生成负荷行。 */
    private List<ApsLoadDO> buildLoadRows(Long versionId, Map<ResourceDayKey, Integer> capacityMap) {
        // 用 LinkedHashMap 保证输出顺序稳定，便于比对两次刷新的结果
        Map<ResourceDayKey, ApsLoadDO> rowMap = new LinkedHashMap<>();
        for (ApsTaskDO task : taskMapper.selectListByVersionId(versionId)) {
            LocalDate workDate = task.getStartTime().toLocalDate();
            ResourceDayKey key = new ResourceDayKey(task.getResId(), workDate);
            ApsLoadDO row = rowMap.computeIfAbsent(key, ignored -> ApsLoadDO.builder()
                    .versionId(versionId)
                    .resId(task.getResId())
                    .resCode(task.getResCode())
                    // 资源名称快照在任务上没有，用编码兜底，避免落 NULL 违反非空约束
                    .resName(task.getResCode())
                    .workDate(workDate)
                    .loadMinutes(0)
                    .capacityMinutes(capacityMap.getOrDefault(key, 0))
                    .build());
            row.setLoadMinutes(row.getLoadMinutes() + task.getDurationMinutes());
        }
        List<ApsLoadDO> rows = new ArrayList<>(rowMap.values());
        rows.forEach(this::fillRate);
        return rows;
    }

    /**
     * 计算负荷率与超负荷分钟数。
     *
     * <p>可用分钟为 0（当天没排班）时负荷率记 0：除零没有业务含义，超负荷分钟数本身已经能表达“排到了休息日”。
     */
    private void fillRate(ApsLoadDO row) {
        int capacity = row.getCapacityMinutes();
        row.setOverloadMinutes(Math.max(row.getLoadMinutes() - capacity, 0));
        row.setLoadRate(capacity == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(row.getLoadMinutes())
                        .divide(BigDecimal.valueOf(capacity), ApsConstants.LOAD_RATE_SCALE, RoundingMode.HALF_UP));
    }

    /**
     * 负荷的汇总维度：资源 + 日期。
     *
     * <p>资源编号是雪花 ID，把它和日期算进同一个数字必然溢出，所以用 record 做复合键。
     */
    private record ResourceDayKey(Long resId, LocalDate workDate) {
    }

}
