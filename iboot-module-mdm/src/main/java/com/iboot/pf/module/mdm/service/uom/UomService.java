package com.iboot.pf.module.mdm.service.uom;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;

import java.util.Collection;
import java.util.List;

public interface UomService {
    Long create(UomSaveReqVO reqVO);

    void update(UomSaveReqVO reqVO);

    void delete(Long id);

    UomDO get(Long id);

    PageResult<UomDO> getPage(UomPageReqVO reqVO);

    /**
     * 获得全部计量单位（含停用）
     *
     * @return 计量单位列表
     */
    List<UomDO> getUomList();

    /**
     * 按编号批量查询计量单位
     *
     * @param ids 计量单位编号集合
     * @return 计量单位列表；空集合返回空列表
     */
    List<UomDO> getUomList(Collection<Long> ids);
}
