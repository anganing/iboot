package com.iboot.pf.module.wms.service.invbalance;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.framework.common.pojo.PageResult;

import com.iboot.pf.module.mdm.api.project.ProjectApi;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvBalanceMapper;
import com.iboot.pf.module.wms.enums.WmsConstants;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.MapUtils.findAndThen;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.INV_BALANCE_NOT_EXISTS;

/**
 * WMS-多维库存余额 Service 实现类。
 *
 * <p>余额只读：写入路径只有 {@code WmsInventoryService#post} 过账，这里不提供任何写方法。
 *
 * @author iboot
 */
@Service
@Validated
public class InvBalanceServiceImpl implements InvBalanceService {

    @Resource
    private WmsInvBalanceMapper invBalanceMapper;
    @Resource
    private ProjectApi projectApi;

    @Override
    public InvBalanceRespVO getInvBalance(Long id) {
        InvBalanceRespVO balance = invBalanceMapper.selectJoinById(id);
        if (balance == null) {
            throw exception(INV_BALANCE_NOT_EXISTS);
        }
        fillProjectName(Collections.singletonList(balance));
        return balance;
    }

    @Override
    public PageResult<InvBalanceRespVO> getInvBalancePage(InvBalancePageReqVO pageReqVO) {
        PageResult<InvBalanceRespVO> pageResult = invBalanceMapper.selectPage(pageReqVO);
        fillProjectName(pageResult.getList());
        return pageResult;
    }

    /**
     * 批量补全归属项目名称。项目属于 mdm，只能走门面批量取，不能在 SQL 里跨库 JOIN；
     * 补全放在 Service 而不是 Controller，Excel 导出复用同一份结果才不会丢字段。
     *
     * <p>项目编号为 {@link WmsConstants#NONE_ID} 表示无项目，不参与查询。
     */
    private void fillProjectName(List<InvBalanceRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> projectIds = list.stream()
                .map(InvBalanceRespVO::getProjectId)
                .filter(projectId -> projectId != null && !WmsConstants.NONE_ID.equals(projectId))
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(projectIds)) {
            return;
        }
        Map<Long, ProjectRespDTO> projectMap = projectApi.getProjectMap(projectIds);
        list.forEach(vo -> findAndThen(projectMap, vo.getProjectId(), project -> vo.setProjectName(project.getName())));
    }

}
