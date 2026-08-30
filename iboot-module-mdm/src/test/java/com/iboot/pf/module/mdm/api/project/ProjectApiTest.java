package com.iboot.pf.module.mdm.api.project;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.project.dto.ProjectRespDTO;
import com.iboot.pf.module.mdm.api.project.dto.ProjectTaskRespDTO;
import com.iboot.pf.module.mdm.dal.dataobject.project.ProjectDO;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.module.mdm.dal.mysql.project.ProjectMapper;
import com.iboot.pf.module.mdm.dal.mysql.projecttask.ProjectTaskMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import com.iboot.pf.module.mdm.service.project.ProjectServiceImpl;
import com.iboot.pf.module.mdm.service.projecttask.ProjectTaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ProjectApiImpl.class, ProjectServiceImpl.class, ProjectTaskServiceImpl.class})
class ProjectApiTest extends BaseDbUnitTest {

    @Resource
    private ProjectApi projectApi;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectTaskMapper projectTaskMapper;
    @MockitoBean
    private MdmCodeService mdmCodeService;

    @BeforeEach
    void setUp() {
        projectMapper.insert(ProjectDO.builder().id(1L).code("PRJ-001").name("项目").status(0).build());
        projectTaskMapper.insert(ProjectTaskDO.builder().id(1L).projectId(1L).code("T-001").name("任务").status(0).build());
    }

    @Test
    void testGetProjectList() {
        List<ProjectRespDTO> list = projectApi.getProjectList();
        assertEquals(1, list.size());
        assertEquals("PRJ-001", list.get(0).getCode());
    }

    @Test
    void testGetProjectTaskList_returnsMatch() {
        List<ProjectTaskRespDTO> list = projectApi.getProjectTaskList(1L);
        assertEquals(1, list.size());
        assertEquals("T-001", list.get(0).getCode());
    }

    @Test
    void testGetProjectTaskList_noTask_returnsEmpty() {
        assertTrue(projectApi.getProjectTaskList(999L).isEmpty());
    }
}
