package com.iboot.pf.module.mes.service.wiptxn;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnPageReqVO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;

/**
 * MES-在制流转流水 Service 接口
 *
 * <p>账表只读：流水是开工、报工、流转、跳序、完工留下的证据链，
 * 放开人工新增修改删除等于允许篡改在制履历，所以这里只有查询方法。
 *
 * @author iboot
 */
public interface WipTxnService {

    /**
     * 获得在制流转流水
     *
     * @param id 编号
     * @return 在制流转流水
     */
    WipTxnRespVO getWipTxn(Long id);

    /**
     * 获得在制流转流水分页
     *
     * @param pageReqVO 分页查询
     * @return 在制流转流水分页
     */
    PageResult<WipTxnRespVO> getWipTxnPage(WipTxnPageReqVO pageReqVO);

}
