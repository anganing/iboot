package com.iboot.pf.module.mdm.service.changeover;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverRespVO;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.ResChangeoverSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.changeover.ResChangeoverDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 资源换型矩阵 Service 接口
 *
 * @author iboot
 */
public interface ResChangeoverService {

    /**
     * 创建换型矩阵
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createResChangeover(@Valid ResChangeoverSaveReqVO createReqVO);

    /**
     * 更新换型矩阵
     *
     * @param updateReqVO 更新信息
     */
    void updateResChangeover(@Valid ResChangeoverSaveReqVO updateReqVO);

    /**
     * 删除换型矩阵
     *
     * @param id 编号
     */
    void deleteResChangeover(Long id);

    /**
     * 批量删除换型矩阵
     *
     * @param ids 编号列表
     */
    void deleteResChangeoverListByIds(List<Long> ids);

    /**
     * 获得换型矩阵
     *
     * @param id 编号
     * @return 换型矩阵
     */
    ResChangeoverRespVO getResChangeover(Long id);

    /**
     * 获得换型矩阵分页
     *
     * @param pageReqVO 分页查询
     * @return 换型矩阵分页
     */
    PageResult<ResChangeoverRespVO> getResChangeoverPage(ResChangeoverPageReqVO pageReqVO);

    /**
     * 按资源编号或资源组批量查询换型矩阵
     *
     * <p>resId = 0 表示按资源组生效。两者任一为空则忽略对应条件。
     *
     * @param resIds 资源编号集合
     * @param resGroups 资源组集合
     * @return 换型矩阵列表；仅返回启用记录
     */
    List<ResChangeoverDO> getChangeoverList(Collection<Long> resIds, Collection<String> resGroups);

}
