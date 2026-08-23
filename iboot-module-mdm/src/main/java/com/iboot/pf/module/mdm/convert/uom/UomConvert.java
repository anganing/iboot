package com.iboot.pf.module.mdm.convert.uom;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomRespVO;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.uom.UomDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UomConvert {
    UomConvert INSTANCE = Mappers.getMapper(UomConvert.class);
    UomDO convert(UomSaveReqVO bean);
    UomRespVO convert(UomDO bean);
    PageResult<UomRespVO> convertPage(PageResult<UomDO> page);
}
