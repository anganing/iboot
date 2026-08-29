package com.iboot.pf.module.mdm.service.esop;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.common.util.http.HttpUtils;
import com.iboot.pf.module.infra.api.file.FileApi;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopFileReqVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopFileRespVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopRespVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopFileMapper;
import com.iboot.pf.module.mdm.dal.mysql.esop.RouteOpEsopMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteMapper;
import com.iboot.pf.module.mdm.dal.mysql.route.RouteOpMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_ENABLED_LOCKED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_ESOP_CONTENT_REQUIRED;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_ESOP_NOT_EXISTS;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_ESOP_SEQ_DUPLICATE;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.ROUTE_OP_NOT_EXISTS;

/**
 * 工序 ESOP 作业步骤 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class RouteOpEsopServiceImpl implements RouteOpEsopService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mov", "wmv", "mkv", "flv", "webm");
    private static final Set<String> DRAWING_EXTENSIONS = Set.of("dwg", "dxf", "step", "stp", "igs", "iges");
    private static final Set<String> OFFICE_EXTENSIONS =
            Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv");

    @Resource
    private RouteOpEsopMapper routeOpEsopMapper;
    @Resource
    private RouteOpEsopFileMapper routeOpEsopFileMapper;
    @Resource
    private RouteOpMapper routeOpMapper;
    @Resource
    private RouteMapper routeMapper;
    @Resource
    private FileApi fileApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRouteOpEsop(RouteOpEsopSaveReqVO createReqVO) {
        validateRouteEditableByRouteOpId(createReqVO.getRouteOpId());
        validateEsopContent(createReqVO);
        validateSeqUnique(createReqVO.getRouteOpId(), createReqVO.getSeq(), null);
        RouteOpEsopDO esop = BeanUtils.toBean(createReqVO, RouteOpEsopDO.class);
        esop.setId(null);
        routeOpEsopMapper.insert(esop);
        replaceEsopFiles(esop.getId(), createReqVO.getFiles());
        return esop.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRouteOpEsop(RouteOpEsopSaveReqVO updateReqVO) {
        RouteOpEsopDO existing = validateRouteOpEsopExists(updateReqVO.getId());
        // 更新时以库中既属工序为准校验锁定，避免通过改外键绕开
        validateRouteEditableByRouteOpId(existing.getRouteOpId());
        if (updateReqVO.getRouteOpId() != null
                && !updateReqVO.getRouteOpId().equals(existing.getRouteOpId())) {
            validateRouteEditableByRouteOpId(updateReqVO.getRouteOpId());
        }
        validateEsopContent(updateReqVO);
        validateSeqUnique(updateReqVO.getRouteOpId(), updateReqVO.getSeq(), updateReqVO.getId());
        RouteOpEsopDO updateObj = BeanUtils.toBean(updateReqVO, RouteOpEsopDO.class);
        routeOpEsopMapper.updateById(updateObj);
        replaceEsopFiles(updateReqVO.getId(), updateReqVO.getFiles());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOpEsop(Long id) {
        RouteOpEsopDO existing = validateRouteOpEsopExists(id);
        validateRouteEditableByRouteOpId(existing.getRouteOpId());
        routeOpEsopFileMapper.deleteByEsopIds(List.of(id));
        routeOpEsopMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouteOpEsopListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteRouteOpEsop(id);
        }
    }

    private RouteOpEsopDO validateRouteOpEsopExists(Long id) {
        RouteOpEsopDO esop = routeOpEsopMapper.selectById(id);
        if (esop == null) {
            throw exception(ROUTE_OP_ESOP_NOT_EXISTS);
        }
        return esop;
    }

    /**
     * 作业步骤必须承载内容：作业要点、工艺参数、附件三者至少有一项，避免写入空壳记录。
     */
    private void validateEsopContent(RouteOpEsopSaveReqVO reqVO) {
        boolean hasContent = reqVO.getContent() != null && !reqVO.getContent().isBlank();
        boolean hasParam = reqVO.getParamJson() != null && !reqVO.getParamJson().isBlank();
        boolean hasFile = reqVO.getFiles() != null && !reqVO.getFiles().isEmpty();
        if (!hasContent && !hasParam && !hasFile) {
            throw exception(ROUTE_OP_ESOP_CONTENT_REQUIRED);
        }
    }

    private void validateSeqUnique(Long routeOpId, Integer seq, Long selfId) {
        RouteOpEsopDO existing = routeOpEsopMapper.selectByRouteOpIdAndSeq(routeOpId, seq);
        if (existing != null && !existing.getId().equals(selfId)) {
            throw exception(ROUTE_OP_ESOP_SEQ_DUPLICATE);
        }
    }

    /**
     * 附件全量替换：先按 esop_id 清空再批量插入，避免逐行 diff 带来的顺序漂移。
     */
    private void replaceEsopFiles(Long esopId, List<RouteOpEsopFileReqVO> files) {
        routeOpEsopFileMapper.deleteByEsopIds(List.of(esopId));
        if (files == null || files.isEmpty()) {
            return;
        }
        List<RouteOpEsopFileDO> fileList = new ArrayList<>(files.size());
        for (int index = 0; index < files.size(); index++) {
            RouteOpEsopFileReqVO file = files.get(index);
            fileList.add(RouteOpEsopFileDO.builder()
                    .esopId(esopId)
                    .seq(index + 1)
                    .fileType(resolveFileType(file.getFileName()))
                    .fileName(file.getFileName())
                    // 只存去掉签名参数的对象地址：预签名 URL 24 小时过期，不能作为主数据长期引用
                    .fileUrl(HttpUtils.removeUrlQuery(file.getFileUrl()))
                    .fileId(file.getFileId())
                    .build());
        }
        routeOpEsopFileMapper.insertBatch(fileList);
    }

    /**
     * 文件类型统一由后端按扩展名判定，避免前后端两处映射表漂移。
     */
    private String resolveFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "OTHER";
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return "IMAGE";
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return "VIDEO";
        }
        if ("pdf".equals(extension)) {
            return "PDF";
        }
        if (DRAWING_EXTENSIONS.contains(extension)) {
            return "DRAWING";
        }
        if (OFFICE_EXTENSIONS.contains(extension)) {
            return "OFFICE";
        }
        return "OTHER";
    }

    /**
     * ESOP 挂载点与路线版本冻结机制耦合：route 启用后禁止增删改作业步骤及其附件。
     */
    private void validateRouteEditableByRouteOpId(Long routeOpId) {
        RouteOpDO routeOp = routeOpMapper.selectById(routeOpId);
        if (routeOp == null) {
            throw exception(ROUTE_OP_NOT_EXISTS);
        }
        RouteDO route = routeMapper.selectById(routeOp.getRouteId());
        if (route == null) {
            throw exception(ROUTE_NOT_EXISTS);
        }
        if (CommonStatusEnum.isEnable(route.getStatus())) {
            throw exception(ROUTE_ENABLED_LOCKED);
        }
    }

    @Override
    public RouteOpEsopRespVO getRouteOpEsop(Long id) {
        RouteOpEsopRespVO esop = routeOpEsopMapper.selectJoinById(id);
        signFileUrls(esop);
        return esop;
    }

    @Override
    public PageResult<RouteOpEsopRespVO> getRouteOpEsopPage(PageParam pageParam, RouteOpEsopPageReqVO pageReqVO) {
        return routeOpEsopMapper.selectJoinPage(pageParam, pageReqVO);
    }

    @Override
    public List<RouteOpEsopRespVO> getRouteOpEsopListByRouteOpId(Long routeOpId) {
        List<RouteOpEsopRespVO> list = routeOpEsopMapper.selectJoinListByRouteOpId(routeOpId);
        list.forEach(esop -> {
            esop.setFileCount(esop.getFiles() == null ? 0 : esop.getFiles().size());
            signFileUrls(esop);
        });
        return list;
    }

    /**
     * 附件存的是去签名的对象地址，读取时重新签发短期访问地址，避免指导书隔天打不开。
     */
    private void signFileUrls(RouteOpEsopRespVO esop) {
        if (esop == null || esop.getFiles() == null) {
            return;
        }
        for (RouteOpEsopFileRespVO file : esop.getFiles()) {
            if (file.getFileUrl() == null || file.getFileUrl().isBlank()) {
                continue;
            }
            file.setFileUrl(fileApi.presignGetUrl(file.getFileUrl(), null));
        }
    }

}
