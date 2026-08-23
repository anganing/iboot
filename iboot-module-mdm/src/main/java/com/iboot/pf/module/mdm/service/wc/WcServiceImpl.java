package com.iboot.pf.module.mdm.service.wc;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 工作中心 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WcServiceImpl implements WcService {

    @Resource
    private WcMapper wcMapper;
    @Resource
    private com.iboot.pf.module.mdm.service.coderule.MdmCodeService mdmCodeService;
    @Resource
    private WorkshopMapper workshopMapper;

    @Override
    public Long createWc(WcSaveReqVO createReqVO) {
        validateWorkshopExists(createReqVO.getWorkshopId());
        // 插入
        WcDO wc = BeanUtils.toBean(createReqVO, WcDO.class);
        wc.setId(null);
        wc.setCode(mdmCodeService.resolveCode(wc.getCode(), "MDM_WC_CODE"));
        wcMapper.insert(wc);

        // 返回
        return wc.getId();
    }

    @Override
    public void updateWc(WcSaveReqVO updateReqVO) {
        // 校验存在
        validateWcExists(updateReqVO.getId());
        validateWorkshopExists(updateReqVO.getWorkshopId());
        // 更新
        WcDO updateObj = BeanUtils.toBean(updateReqVO, WcDO.class);
        wcMapper.updateById(updateObj);
    }

    @Override
    public void deleteWc(Long id) {
        // 校验存在
        validateWcExists(id);
        // 删除
        wcMapper.deleteById(id);
    }

    @Override
        public void deleteWcListByIds(List<Long> ids) {
        // 删除
        wcMapper.deleteByIds(ids);
        }


    private void validateWcExists(Long id) {
        if (wcMapper.selectById(id) == null) {
            throw exception(WC_NOT_EXISTS);
        }
    }

    private void validateWorkshopExists(Long workshopId) {
        if (workshopMapper.selectById(workshopId) == null) {
            throw exception(WORKSHOP_NOT_EXISTS);
        }
    }

    @Override
    public WcDO getWc(Long id) {
        return wcMapper.selectById(id);
    }

    @Override
    public PageResult<WcDO> getWcPage(WcPageReqVO pageReqVO) {
        return wcMapper.selectPage(pageReqVO);
    }

}
