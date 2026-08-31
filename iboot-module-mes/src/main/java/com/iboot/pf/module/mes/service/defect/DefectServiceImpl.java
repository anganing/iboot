package com.iboot.pf.module.mes.service.defect;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.http.HttpUtils;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mdm.api.defectcode.dto.DefectCodeRespDTO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectFileReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectFileRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectPageReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectFileDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectFileMapper;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesFileTypeEnum;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_RESULT_TASK_NOT_MATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;

/**
 * MES-缺陷记录 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class DefectServiceImpl implements DefectService {

    @Resource
    private DefectMapper defectMapper;
    @Resource
    private DefectFileMapper defectFileMapper;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private FileApi fileApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDefect(DefectSaveReqVO createReqVO) {
        DefectDO defect = BeanUtils.toBean(createReqVO, DefectDO.class);
        defect.setId(null);
        prepare(defect);
        defectMapper.insert(defect);
        replaceFiles(defect.getId(), createReqVO.getFiles());
        return defect.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefect(DefectSaveReqVO updateReqVO) {
        validateDefectExists(updateReqVO.getId());
        DefectDO defect = BeanUtils.toBean(updateReqVO, DefectDO.class);
        prepare(defect);
        defectMapper.updateById(defect);
        replaceFiles(updateReqVO.getId(), updateReqVO.getFiles());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefect(Long id) {
        validateDefectExists(id);
        // 附件先删再删主记录，避免留下孤儿附件
        defectFileMapper.deleteByDefectIds(List.of(id));
        defectMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefectListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            validateDefectExists(id);
        }
        defectFileMapper.deleteByDefectIds(ids);
        defectMapper.deleteByIds(ids);
    }

    private DefectDO validateDefectExists(Long id) {
        DefectDO defect = id == null ? null : defectMapper.selectById(id);
        if (defect == null) {
            throw exception(DEFECT_NOT_EXISTS);
        }
        return defect;
    }

    /**
     * 落库前的公共处理：校验质检任务与检验结果归属，按缺陷代码补全编码、名称、等级快照。
     */
    private void prepare(DefectDO defect) {
        qcTaskService.validateQcTaskExists(defect.getTaskId());
        defect.setResultId(defect.getResultId() == null ? MesConstants.NONE_ID : defect.getResultId());
        if (!MesConstants.NONE_ID.equals(defect.getResultId())) {
            QcResultDO qcResult = qcResultMapper.selectById(defect.getResultId());
            if (qcResult == null) {
                throw exception(QC_RESULT_NOT_EXISTS);
            }
            // 检验结果必须属于同一质检任务，否则缺陷追溯链是错的
            if (!Objects.equals(qcResult.getTaskId(), defect.getTaskId())) {
                throw exception(DEFECT_RESULT_TASK_NOT_MATCH);
            }
        }
        DefectCodeRespDTO defectCode = snapshotService.requireDefectCode(defect.getDefectCodeId());
        defect.setDefectCode(defectCode.getCode());
        defect.setDefectName(defectCode.getName());
        defect.setDefectLevel(defectCode.getLevel());
    }

    /**
     * 附件全量替换：先按父键删旧行，再按入参下标重排 seq 批量插入。
     *
     * <p>不做逐行 diff：顺序会漂移，复杂度换不来任何收益。落库存去签名地址，
     * 预签名 URL 只有 24 小时有效，直接存库隔天就打不开。
     */
    private void replaceFiles(Long defectId, List<DefectFileReqVO> files) {
        defectFileMapper.deleteByDefectIds(List.of(defectId));
        if (CollUtil.isEmpty(files)) {
            return;
        }
        List<DefectFileDO> fileList = new ArrayList<>(files.size());
        for (int index = 0; index < files.size(); index++) {
            DefectFileReqVO file = files.get(index);
            fileList.add(DefectFileDO.builder()
                    .defectId(defectId)
                    .seq(index + MesConstants.FIRST_LINE_NO)
                    .fileType(MesFileTypeEnum.ofFileName(file.getFileName()))
                    .fileName(file.getFileName())
                    .fileUrl(HttpUtils.removeUrlQuery(file.getFileUrl()))
                    .fileId(file.getFileId())
                    .build());
        }
        defectFileMapper.insertBatch(fileList);
    }

    @Override
    public DefectRespVO getDefect(Long id) {
        DefectRespVO respVO = defectMapper.selectJoinById(id);
        if (respVO == null) {
            return null;
        }
        respVO.setFiles(convertFiles(defectFileMapper.selectListByDefectId(id)));
        return respVO;
    }

    @Override
    public PageResult<DefectRespVO> getDefectPage(DefectPageReqVO pageReqVO) {
        return defectMapper.selectPage(pageReqVO);
    }

    /** 读取时对 fileUrl 重新签发预签名地址，库里存的是去签名地址。 */
    private List<DefectFileRespVO> convertFiles(List<DefectFileDO> files) {
        List<DefectFileRespVO> result = new ArrayList<>(files.size());
        for (DefectFileDO file : files) {
            DefectFileRespVO respVO = BeanUtils.toBean(file, DefectFileRespVO.class);
            if (file.getFileUrl() != null && !file.getFileUrl().isBlank()) {
                respVO.setFileUrl(fileApi.presignGetUrl(file.getFileUrl(), null));
            }
            result.add(respVO);
        }
        return result;
    }

}
