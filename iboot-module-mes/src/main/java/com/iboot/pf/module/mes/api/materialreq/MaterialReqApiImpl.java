package com.iboot.pf.module.mes.api.materialreq;

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqActionReqDTO;
import com.iboot.pf.module.mes.api.materialreq.dto.MaterialReqRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesCreateMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 领退补料申请 API 实现。
 *
 * <p>门面只做转换与转调：建单与三个状态动作全部转调 {@link MesMaterialReqDomainService}——领料每一段都有
 * 库存后果（提交占预留、确认走 WMS 过账、取消释放预留），门面自己碰状态就等于把预留和过账绕过去。
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：MES 领料要占 WMS 的库存，
 * 占的是仓库里客观存在的料，不是"当前登录人所在部门的料"，跨模块调用天生是系统身份。
 */
@Service
@Validated
@DataPermission(enable = false)
public class MaterialReqApiImpl implements MaterialReqApi {

    @Resource
    private MesMaterialReqDomainService materialReqDomainService;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @Override
    public Long createMaterialReq(CreateMaterialReqReqDTO reqDTO) {
        MesCreateMaterialReqBO bo = BeanUtils.toBean(reqDTO, MesCreateMaterialReqBO.class);
        // 明细行显式转一遍而不是靠 BeanUtils 递归猜嵌套泛型：两个 Line 是不同的类，
        // 行上任何一个字段被静默丢掉都会变成"领料数量对不上"，而这类错在单据落库后极难反查。
        // mustIssueBeforeStart 门面 DTO 上没有，留 null 由领域侧按 true 处理（漏发开工前必需的料的代价更大）
        bo.setLines(reqDTO.getLines() == null ? null
                : convertList(reqDTO.getLines(), line -> BeanUtils.toBean(line, MesCreateMaterialReqBO.Line.class)));
        return materialReqDomainService.createMaterialReq(bo);
    }

    @Override
    public void cancelMaterialReq(MaterialReqActionReqDTO reqDTO) {
        materialReqDomainService.cancelMaterialReq(BeanUtils.toBean(reqDTO, MesMaterialReqActionBO.class));
    }

    @Override
    public void confirmMaterialReq(MaterialReqActionReqDTO reqDTO) {
        MesConfirmMaterialReqBO bo = BeanUtils.toBean(reqDTO, MesConfirmMaterialReqBO.class);
        // DTO 叫 reason、BO 叫 remark，显式搬。lines 留空表示全部行按剩余量全额确认，
        // 门面 DTO 没有行级确认量与库位，逐行部分确认只能走端点（见自查偏差）
        bo.setRemark(reqDTO.getReason());
        materialReqDomainService.confirmMaterialReq(bo);
    }

    @Override
    public MaterialReqRespDTO getMaterialReq(Long id) {
        MaterialReqDO req = id == null ? null : materialReqMapper.selectById(id);
        if (req == null) {
            return null;
        }
        // 头 + 行一次带出：让调用方再调一次行接口就是 N+1。两次等值查询，不跨库 JOIN
        List<MaterialReqLineDO> lines = materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, id);
        MaterialReqRespDTO respDTO = BeanUtils.toBean(req, MaterialReqRespDTO.class);
        respDTO.setLines(BeanUtils.toBean(lines, MaterialReqRespDTO.Line.class));
        return respDTO;
    }

}
