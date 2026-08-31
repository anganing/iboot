package com.iboot.pf.module.aps.service.load;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadPageReqVO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadRespVO;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * APS-资源负荷 Service 接口。
 *
 * <p>负荷是派生数据：由“刷新负荷”按当前版本的排程任务与资源日历投影整体重算，
 * 只提供查询、导出与刷新，不提供新增、修改、删除、导入。人工改负荷分钟数会让负荷表和任务对不上。
 *
 * @author iboot
 */
public interface ApsLoadService {

    /**
     * 获得资源负荷
     *
     * @param id 编号
     * @return 资源负荷
     */
    LoadRespVO getLoad(Long id);

    /**
     * 获得资源负荷分页
     *
     * @param pageReqVO 分页查询
     * @return 资源负荷分页
     */
    PageResult<LoadRespVO> getLoadPage(LoadPageReqVO pageReqVO);

    /**
     * 按版本的排程任务与资源日历投影重算负荷
     *
     * @param reqDTO 命令
     */
    void refreshLoad(@Valid RefreshLoadReqDTO reqDTO);

    /**
     * 按版本获得资源负荷列表
     *
     * @param versionId 版本编号
     * @return 负荷列表
     */
    List<ApsLoadDO> getLoadDOListByVersionId(Long versionId);

}
