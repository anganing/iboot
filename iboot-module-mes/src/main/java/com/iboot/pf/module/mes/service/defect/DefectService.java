package com.iboot.pf.module.mes.service.defect;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectPageReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES-缺陷记录 Service 接口
 *
 * @author iboot
 */
public interface DefectService {

    /**
     * 创建缺陷记录，附件一并写入
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDefect(@Valid DefectSaveReqVO createReqVO);

    /**
     * 更新缺陷记录，附件全量替换
     *
     * @param updateReqVO 更新信息
     */
    void updateDefect(@Valid DefectSaveReqVO updateReqVO);

    /**
     * 删除缺陷记录，附件级联清理
     *
     * @param id 编号
     */
    void deleteDefect(Long id);

    /**
     * 批量删除缺陷记录，附件级联清理
     *
     * @param ids 编号
     */
    void deleteDefectListByIds(List<Long> ids);

    /**
     * 获得缺陷记录，含附件明细，文件地址已重新签发
     *
     * @param id 编号
     * @return 缺陷记录
     */
    DefectRespVO getDefect(Long id);

    /**
     * 获得缺陷记录分页，只带附件数量
     *
     * @param pageReqVO 分页查询
     * @return 缺陷记录分页
     */
    PageResult<DefectRespVO> getDefectPage(DefectPageReqVO pageReqVO);

}
