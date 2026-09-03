package com.iboot.pf.module.system.controller.admin.notice;

import cn.hutool.core.lang.Assert;
import com.iboot.pf.framework.common.enums.UserTypeEnum;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.infra.api.websocket.WebSocketSenderApi;
import com.iboot.pf.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.iboot.pf.module.system.controller.admin.notice.vo.NoticeRespVO;
import com.iboot.pf.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import com.iboot.pf.module.system.dal.dataobject.notice.NoticeDO;
import com.iboot.pf.module.system.enums.WebSocketMessageTypeConstants;
import com.iboot.pf.module.system.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 通知公告")
@RestController
@RequestMapping("/system/notice")
@Validated
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @PostMapping("/create")
    @Operation(summary = "创建通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:create')")
    public CommonResult<Long> createNotice(@Valid @RequestBody NoticeSaveReqVO createReqVO) {
        Long noticeId = noticeService.createNotice(createReqVO);
        return success(noticeId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> updateNotice(@Valid @RequestBody NoticeSaveReqVO updateReqVO) {
        noticeService.updateNotice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
        noticeService.deleteNotice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除通知公告")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNoticeList(@RequestParam("ids") List<Long> ids) {
        noticeService.deleteNoticeList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取通知公告列表")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<PageResult<NoticeRespVO>> getNoticePage(@Validated NoticePageReqVO pageReqVO) {
        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, NoticeRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<NoticeRespVO> getNotice(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.getNotice(id);
        return success(BeanUtils.toBean(notice, NoticeRespVO.class));
    }

    @PostMapping("/push")
    @Operation(summary = "推送通知公告", description = "只发送给 websocket 连接在线的用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> push(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.getNotice(id);
        Assert.notNull(notice, "公告不能为空");
        // 通过 websocket 推送给在线的用户。只推 VO，DO 带 creator/deleted 等字段不该出网
        webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(),
                WebSocketMessageTypeConstants.NOTICE_PUSH, BeanUtils.toBean(notice, NoticeRespVO.class));
        return success(true);
    }

    // ========== 查看开启中的公告，所有登录用户可见 ==========

    @GetMapping("/my-page")
    @Operation(summary = "获得我的通知公告分页", description = "只返回开启状态的公告，所有登录用户可见")
    public CommonResult<PageResult<NoticeRespVO>> getMyNoticePage(@Validated PageParam pageParam) {
        PageResult<NoticeDO> pageResult = noticeService.getEnabledNoticePage(pageParam);
        return success(BeanUtils.toBean(pageResult, NoticeRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得我的通知公告", description = "只返回开启状态的公告，所有登录用户可见")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<NoticeRespVO> getMyNotice(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.getEnabledNotice(id);
        return success(BeanUtils.toBean(notice, NoticeRespVO.class));
    }

}
