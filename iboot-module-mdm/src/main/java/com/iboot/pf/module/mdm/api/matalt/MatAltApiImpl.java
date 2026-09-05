package com.iboot.pf.module.mdm.api.matalt;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mdm.api.matalt.dto.MatAltRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import com.iboot.pf.module.mdm.dal.mysql.matalt.MatAltMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 替代料 API 实现类
 *
 * <p>优先级排序在应用层做而不是写进 SQL：一个物料的替代料是个位数，排序代价可忽略，
 * 单查与批量查共用同一个比较器，两个方法的顺序口径不会漂移。
 *
 * @author iboot
 */
@Service
@DataPermission(enable = false)
public class MatAltApiImpl implements MatAltApi {

    /** 优先级为空的关系排在最后：没填优先级意味着没想清楚顺序，不该抢在填了的前面。 */
    private static final Comparator<MatAltRespDTO> BY_PRIORITY =
            Comparator.comparing(MatAltRespDTO::getPriority, Comparator.nullsLast(Comparator.naturalOrder()));

    @Resource
    private MatAltMapper matAltMapper;

    @Override
    public List<MatAltRespDTO> getAltList(Long matId) {
        if (matId == null) {
            return List.of();
        }
        List<MatAltDO> list = matAltMapper.selectList(MatAltDO::getMatId, matId);
        return BeanUtils.toBean(list, MatAltRespDTO.class).stream().sorted(BY_PRIORITY).toList();
    }

    @Override
    public Map<Long, List<MatAltRespDTO>> getAltMap(Collection<Long> matIds) {
        if (matIds == null || matIds.isEmpty()) {
            return Map.of();
        }
        List<MatAltDO> list = matAltMapper.selectList(MatAltDO::getMatId, matIds);
        return BeanUtils.toBean(list, MatAltRespDTO.class).stream()
                .sorted(BY_PRIORITY)
                .collect(Collectors.groupingBy(MatAltRespDTO::getMatId));
    }

}
