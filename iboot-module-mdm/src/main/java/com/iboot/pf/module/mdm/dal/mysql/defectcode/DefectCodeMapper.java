package com.iboot.pf.module.mdm.dal.mysql.defectcode;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodePageReqVO;
import com.iboot.pf.module.mdm.controller.admin.defectcode.vo.DefectCodeRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.defectcode.DefectCodeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 缺陷代码 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DefectCodeMapper extends BaseMapperX<DefectCodeDO> {

    default PageResult<DefectCodeRespVO> selectJoinPage(DefectCodePageReqVO reqVO) {
        IPage<DefectCodeRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DefectCodeRespVO selectJoinById(@Param("id") Long id);

    IPage<DefectCodeRespVO> selectJoinPage(IPage<DefectCodeRespVO> page, @Param("req") DefectCodePageReqVO reqVO);

    default List<DefectCodeDO> selectListByParentId(Long parentId) {
        return selectList(DefectCodeDO::getParentId, parentId);
    }

    default DefectCodeDO selectByCode(String code) {
        return selectOne(DefectCodeDO::getCode, code);
    }

}
