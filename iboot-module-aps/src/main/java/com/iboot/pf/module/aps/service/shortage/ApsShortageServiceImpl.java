package com.iboot.pf.module.aps.service.shortage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.shortage.ApsShortageDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.dal.mysql.shortage.ApsShortageMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.service.command.ApsCommandService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;

@Service
public class ApsShortageServiceImpl implements ApsShortageService {
    @Resource private ApsShortageMapper apsShortageMapper; @Resource private ApsVersionMapper apsVersionMapper;
    @Resource private ApsInventoryProjectionMapper apsInventoryProjectionMapper; @Resource private ApsCommandService commandService;
    @Override public List<ApsShortageDO> getShortageListByVersionId(Long id) { return apsShortageMapper.selectList(ApsShortageDO::getVersionId, id); }
    @Override @Transactional(rollbackFor = Exception.class)
    public void refreshShortage(RefreshShortageReqDTO reqDTO) {
        commandService.executeVoid("REFRESH_SHORTAGE", reqDTO, () -> {
            ApsVersionDO version = apsVersionMapper.selectById(reqDTO.getVersionId()); if (version == null) throw exception(VERSION_NOT_EXISTS);
            Map<Long, BigDecimal> availableMap = apsInventoryProjectionMapper.selectList(ApsInventoryProjectionDO::getScenarioId, version.getScenarioId()).stream()
                    .filter(item -> "QUALIFIED".equals(item.getQualityStatus()))
                    .collect(Collectors.groupingBy(ApsInventoryProjectionDO::getMatId,
                            Collectors.reducing(BigDecimal.ZERO, ApsInventoryProjectionDO::getQtyAvailable, BigDecimal::add)));
            List<ApsShortageDO> rows = getShortageListByVersionId(reqDTO.getVersionId());
            rows.forEach(row -> { BigDecimal available = availableMap.getOrDefault(row.getMatId(), BigDecimal.ZERO);
                BigDecimal shortage = row.getReqQty().subtract(available).max(BigDecimal.ZERO); row.setAvailQty(available);
                row.setShortQty(shortage); row.setStatus(shortage.signum() == 0 ? "RESOLVED" : available.signum() > 0 ? "PARTIAL" : "OPEN"); });
            if (!rows.isEmpty()) apsShortageMapper.updateBatch(rows);
        });
    }
}
