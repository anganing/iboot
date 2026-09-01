package com.iboot.pf.module.mes.service.defect;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectFileReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectPageReqVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectRespVO;
import com.iboot.pf.module.mes.controller.admin.defect.vo.DefectSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectDO;
import com.iboot.pf.module.mes.dal.dataobject.defect.DefectFileDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectFileMapper;
import com.iboot.pf.module.mes.dal.mysql.defect.DefectMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDefectLevelEnum;
import com.iboot.pf.module.mes.enums.MesFileTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DEFECT_RESULT_TASK_NOT_MATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RESULT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 缺陷记录 Service 测试：缺陷代码快照落库、检验结果归属校验、附件全量替换与去签名地址落库、删除级联清理附件。
 */
@Import(DefectServiceImpl.class)
class DefectServiceImplTest extends BaseDbUnitTest {

    private static final Long TASK_ID = 1L;
    private static final Long OTHER_TASK_ID = 2L;

    @Resource
    private DefectService defectService;
    @Resource
    private DefectMapper defectMapper;
    @Resource
    private DefectFileMapper defectFileMapper;
    @Resource
    private QcResultMapper qcResultMapper;

    @MockitoBean
    private QcTaskService qcTaskService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private FileApi fileApi;

    @BeforeEach
    void mockExternalDependencies() {
        when(qcTaskService.validateQcTaskExists(any())).thenReturn(new QcTaskDO()
                .setId(TASK_ID).setStatus(MesQcTaskStatusEnum.INSPECTING.name()));
        when(snapshotService.requireDefectCode(any())).thenReturn(MesTestFixtures.defectCode());
        // 库里存去签名地址，读取时重新签发，预签名 URL 只有 24 小时有效
        when(fileApi.presignGetUrl(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0) + "?sign=fresh");
    }

    private DefectSaveReqVO buildReqVO(Long resultId) {
        DefectSaveReqVO reqVO = new DefectSaveReqVO();
        reqVO.setTaskId(TASK_ID);
        reqVO.setResultId(resultId);
        reqVO.setDefectCodeId(51L);
        reqVO.setQty(new BigDecimal("2"));
        reqVO.setDescription("绕线端部松动");
        reqVO.setLocation("A 面第三槽");
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    private DefectFileReqVO buildFile(String fileName, String fileUrl) {
        DefectFileReqVO file = new DefectFileReqVO();
        file.setFileName(fileName);
        file.setFileUrl(fileUrl);
        file.setFileId(1L);
        return file;
    }

    @Test
    void testCreateDefect_defectCodeSnapshotFilled() {
        Long id = defectService.createDefect(buildReqVO(MesConstants.NONE_ID));

        DefectDO defect = defectMapper.selectById(id);
        assertNotNull(defect);
        // 编码、名称、等级都是 mdm 缺陷代码库的快照，前端只选缺陷代码
        assertEquals("DEF-001", defect.getDefectCode());
        assertEquals("绕线松动", defect.getDefectName());
        assertEquals(MesDefectLevelEnum.MAJOR.name(), defect.getDefectLevel());
    }

    @Test
    void testCreateDefect_resultNotExists() {
        assertServiceException(() -> defectService.createDefect(buildReqVO(randomLongId())), QC_RESULT_NOT_EXISTS);
    }

    @Test
    void testCreateDefect_resultTaskNotMatch() {
        QcResultDO qcResult = new QcResultDO().setTaskId(OTHER_TASK_ID).setItemId(1L).setSampleNo(1);
        qcResultMapper.insert(qcResult);

        // 检验结果必须属于同一质检任务，否则缺陷追溯链是错的
        assertServiceException(() -> defectService.createDefect(buildReqVO(qcResult.getId())),
                DEFECT_RESULT_TASK_NOT_MATCH);
    }

    @Test
    void testCreateDefect_withFiles() {
        DefectSaveReqVO reqVO = buildReqVO(MesConstants.NONE_ID);
        reqVO.setFiles(List.of(
                buildFile("缺陷照片.jpg", "http://minio/mes/defect/1.jpg?sign=old"),
                buildFile("检验报告.pdf", "http://minio/mes/defect/1.pdf")));

        Long id = defectService.createDefect(reqVO);

        List<DefectFileDO> files = defectFileMapper.selectListByDefectId(id);
        assertEquals(2, files.size());
        // 行号按入参下标重排，从 1 开始
        assertEquals(MesConstants.FIRST_LINE_NO, files.get(0).getSeq());
        // 落库存去签名地址，否则隔天就打不开
        assertEquals("http://minio/mes/defect/1.jpg", files.get(0).getFileUrl());
        // 文件类型按文件名判定
        assertEquals(MesFileTypeEnum.IMAGE.name(), files.get(0).getFileType());
    }

    @Test
    void testUpdateDefect_filesReplacedEntirely() {
        DefectSaveReqVO createReqVO = buildReqVO(MesConstants.NONE_ID);
        createReqVO.setFiles(List.of(buildFile("旧照片.jpg", "http://minio/mes/defect/old.jpg")));
        Long id = defectService.createDefect(createReqVO);

        DefectSaveReqVO updateReqVO = buildReqVO(MesConstants.NONE_ID);
        updateReqVO.setId(id);
        updateReqVO.setFiles(List.of(buildFile("新照片.jpg", "http://minio/mes/defect/new.jpg")));
        defectService.updateDefect(updateReqVO);

        List<DefectFileDO> files = defectFileMapper.selectListByDefectId(id);
        // 附件走全量替换，不做逐行 diff
        assertEquals(1, files.size());
        assertEquals("新照片.jpg", files.get(0).getFileName());
    }

    @Test
    void testUpdateDefect_notExists() {
        DefectSaveReqVO updateReqVO = buildReqVO(MesConstants.NONE_ID);
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> defectService.updateDefect(updateReqVO), DEFECT_NOT_EXISTS);
    }

    @Test
    void testDeleteDefect_cascadeFiles() {
        DefectSaveReqVO createReqVO = buildReqVO(MesConstants.NONE_ID);
        createReqVO.setFiles(List.of(buildFile("缺陷照片.jpg", "http://minio/mes/defect/1.jpg")));
        Long id = defectService.createDefect(createReqVO);

        defectService.deleteDefect(id);

        assertNull(defectMapper.selectById(id));
        // 附件先删再删主记录，避免留下孤儿附件
        assertTrue(defectFileMapper.selectListByDefectId(id).isEmpty());
    }

    @Test
    void testDeleteDefect_notExists() {
        assertServiceException(() -> defectService.deleteDefect(randomLongId()), DEFECT_NOT_EXISTS);
    }

    @Test
    void testDeleteDefectList_cascadeFiles() {
        DefectSaveReqVO first = buildReqVO(MesConstants.NONE_ID);
        first.setFiles(List.of(buildFile("a.jpg", "http://minio/a.jpg")));
        Long firstId = defectService.createDefect(first);
        Long secondId = defectService.createDefect(buildReqVO(MesConstants.NONE_ID));

        defectService.deleteDefectListByIds(List.of(firstId, secondId));

        assertNull(defectMapper.selectById(firstId));
        assertNull(defectMapper.selectById(secondId));
        assertTrue(defectFileMapper.selectListByDefectId(firstId).isEmpty());
    }

    @Test
    void testGetDefect_fileUrlResigned() {
        DefectSaveReqVO createReqVO = buildReqVO(MesConstants.NONE_ID);
        createReqVO.setFiles(List.of(buildFile("缺陷照片.jpg", "http://minio/mes/defect/1.jpg")));
        Long id = defectService.createDefect(createReqVO);

        DefectRespVO respVO = defectService.getDefect(id);

        assertNotNull(respVO);
        assertEquals(1, respVO.getFiles().size());
        // 读取时重新签发，前端拿到的才是可访问地址
        assertEquals("http://minio/mes/defect/1.jpg?sign=fresh", respVO.getFiles().get(0).getFileUrl());
    }

    @Test
    void testGetDefectPage() {
        defectService.createDefect(buildReqVO(MesConstants.NONE_ID));

        DefectPageReqVO reqVO = new DefectPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<DefectRespVO> list = defectService.getDefectPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("DEF-001", list.get(0).getDefectCode());
    }

}
