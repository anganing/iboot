package com.iboot.pf.module.mdm.service.defectcode;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodePageReqVO;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeRespVO;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 缺陷代码 Service 接口
 *
 * @author iboot
 */
public interface DefectCodeService {

    /**
     * 创建缺陷代码
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDefectCode(@Valid DefectCodeSaveReqVO createReqVO);

    /**
     * 更新缺陷代码
     *
     * @param updateReqVO 更新信息
     */
    void updateDefectCode(@Valid DefectCodeSaveReqVO updateReqVO);

    /**
     * 删除缺陷代码
     *
     * @param id 编号
     */
    void deleteDefectCode(Long id);

    /**
     * 批量删除缺陷代码
     *
     * @param ids 编号列表
     */
    void deleteDefectCodeListByIds(List<Long> ids);

    /**
     * 获得缺陷代码
     *
     * @param id 编号
     * @return 缺陷代码
     */
    DefectCodeRespVO getDefectCode(Long id);

    /**
     * 获得缺陷代码分页
     *
     * @param pageReqVO 分页查询
     * @return 缺陷代码分页
     */
    PageResult<DefectCodeRespVO> getDefectCodePage(DefectCodePageReqVO pageReqVO);

    /**
     * 获得全部缺陷代码（含停用，供门面批量查询与树构建）
     *
     * @return 缺陷代码列表
     */
    List<DefectCodeDO> getDefectCodeList();

}
