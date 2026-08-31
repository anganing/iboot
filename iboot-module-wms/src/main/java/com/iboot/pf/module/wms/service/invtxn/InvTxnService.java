package com.iboot.pf.module.wms.service.invtxn;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invtxn.InvTxnDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * WMS-库存流水 Service 接口
 *
 * @author iboot
 */
public interface InvTxnService {

    /**
     * 获得WMS-库存流水
     *
     * @param id 编号
     * @return WMS-库存流水
     */
    InvTxnRespVO getInvTxn(Long id);

    /**
     * 获得WMS-库存流水分页
     *
     * @param pageReqVO 分页查询
     * @return WMS-库存流水分页
     */
    PageResult<InvTxnRespVO> getInvTxnPage(InvTxnPageReqVO pageReqVO);

}
