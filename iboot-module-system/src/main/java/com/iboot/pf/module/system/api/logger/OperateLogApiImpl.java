package com.iboot.pf.module.system.api.logger;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.iboot.pf.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.iboot.pf.module.system.api.logger.dto.OperateLogRespDTO;
import com.iboot.pf.module.system.dal.dataobject.logger.OperateLogDO;
import com.iboot.pf.module.system.service.logger.OperateLogService;
import org.dromara.core.trans.anno.TransMethodResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 操作日志 API 实现类
 *
 * @author iboot
 */
@Service
@Validated
@DataPermission(enable = false)
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogService.createOperateLog(createReqDTO);
    }

    @Override
    @TransMethodResult
    public PageResult<OperateLogRespDTO> getOperateLogPage(OperateLogPageReqDTO pageReqDTO) {
        PageResult<OperateLogDO> operateLogPage = operateLogService.getOperateLogPage(pageReqDTO);
        return BeanUtils.toBean(operateLogPage, OperateLogRespDTO.class);
    }

}
