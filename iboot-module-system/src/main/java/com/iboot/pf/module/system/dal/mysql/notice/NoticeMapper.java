package com.iboot.pf.module.system.dal.mysql.notice;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.iboot.pf.module.system.dal.dataobject.notice.NoticeDO;
import org.apache.ibatis.annotations.Mapper;

import com.iboot.pf.framework.common.pojo.PageParam;

@Mapper
public interface NoticeMapper extends BaseMapperX<NoticeDO> {

    default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())
                .orderByDesc(NoticeDO::getId));
    }

    /**
     * 查询指定状态的公告分页，给所有登录用户看
     */
    default PageResult<NoticeDO> selectPageByStatus(PageParam reqVO, Integer status) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .eq(NoticeDO::getStatus, status)
                .orderByDesc(NoticeDO::getId));
    }

    /**
     * 查询指定状态的单条公告，避免越权读到已关闭的公告
     */
    default NoticeDO selectByIdAndStatus(Long id, Integer status) {
        return selectOne(new LambdaQueryWrapperX<NoticeDO>()
                .eq(NoticeDO::getId, id)
                .eq(NoticeDO::getStatus, status));
    }

}
