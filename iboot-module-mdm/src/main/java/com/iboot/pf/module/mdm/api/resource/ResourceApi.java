package com.iboot.pf.module.mdm.api.resource;

import com.iboot.pf.module.mdm.api.resource.dto.ResChangeoverRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCalendarRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceCapabilityRespDTO;
import com.iboot.pf.module.mdm.api.resource.dto.ResourceRespDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 生产资源 API 接口
 *
 * <p>对 MES / APS 提供资源、日历、能力、换型门面。
 *
 * @author iboot
 */
public interface ResourceApi {

    /**
     * 按编号单查生产资源
     *
     * @param id 资源编号
     * @return 生产资源；不存在返回 {@code null}
     */
    ResourceRespDTO getResource(Long id);

    /**
     * 按编号批量查询生产资源
     *
     * @param ids 资源编号集合
     * @return 资源列表；空集合返回空列表
     */
    List<ResourceRespDTO> getResourceList(Collection<Long> ids);

    /**
     * 按编号批量查询生产资源 Map，供批量补全
     *
     * @param ids 资源编号集合
     * @return 资源编号 -> 资源；缺失编号不产生键
     */
    Map<Long, ResourceRespDTO> getResourceMap(Collection<Long> ids);

    /**
     * 按资源编号与日期区间（闭区间）批量查询资源日历
     *
     * @param resIds 资源编号集合
     * @param startDate 起始日期（含）
     * @param endDate 结束日期（含）
     * @return 资源日历列表
     */
    List<ResourceCalendarRespDTO> getResourceCalendarList(Collection<Long> resIds,
                                                          LocalDate startDate,
                                                          LocalDate endDate);

    /**
     * 按标准工序编号集合批量查询启用中的资源能力
     *
     * @param opIds 标准工序编号集合
     * @return 资源能力列表；仅返回启用记录
     */
    List<ResourceCapabilityRespDTO> getResourceCapabilityList(Collection<Long> opIds);

    /**
     * 按资源编号或资源组批量查询换型矩阵
     *
     * @param resIds 资源编号集合
     * @param resGroups 资源组集合
     * @return 换型矩阵列表；仅返回启用记录
     */
    List<ResChangeoverRespDTO> getChangeoverList(Collection<Long> resIds, Collection<String> resGroups);

}
