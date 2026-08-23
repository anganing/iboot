package com.iboot.pf.module.mdm.service.uom;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;

public interface UomService {
    Long create(UomSaveReqVO reqVO);

    void update(UomSaveReqVO reqVO);

    void delete(Long id);

    UomDO get(Long id);

    PageResult<UomDO> getPage(UomPageReqVO reqVO);
}
