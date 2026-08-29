package com.iboot.pf.module.mdm.dal.mysql.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.ToolRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.ToolDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 工装模具刀具 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ToolMapper extends BaseMapperX<ToolDO> {

    default PageResult<ToolRespVO> selectJoinPage(ToolPageReqVO reqVO) {
        IPage<ToolRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ToolRespVO selectJoinById(@Param("id") Long id);

    IPage<ToolRespVO> selectJoinPage(IPage<ToolRespVO> page, @Param("req") ToolPageReqVO reqVO);

    default ToolDO selectByCode(String code) {
        return selectOne(ToolDO::getCode, code);
    }

}
