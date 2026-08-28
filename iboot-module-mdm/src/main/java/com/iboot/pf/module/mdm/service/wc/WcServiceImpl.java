package com.iboot.pf.module.mdm.service.wc;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.WcPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.WcRespVO;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.WcSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.dal.mysql.workshop.WorkshopMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.WC_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.WORKSHOP_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.WC;

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
    private MdmCodeService mdmCodeService;
    @Resource
    private WorkshopMapper workshopMapper;

    @Override
    public Long createWc(WcSaveReqVO createReqVO) {
        validateWorkshopExists(createReqVO.getWorkshopId());
        // 插入
        WcDO wc = BeanUtils.toBean(createReqVO, WcDO.class);
        wc.setId(null);
        wc.setCode(mdmCodeService.resolveCode(wc.getCode(), WC));
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
    public WcRespVO getWc(Long id) {
        return wcMapper.selectJoinById(id);
    }

    @Override
    public PageResult<WcRespVO> getWcPage(WcPageReqVO pageReqVO) {
        return wcMapper.selectPage(pageReqVO);
    }

}
