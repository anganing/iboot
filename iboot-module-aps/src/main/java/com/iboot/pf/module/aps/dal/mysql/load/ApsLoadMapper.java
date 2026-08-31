package com.iboot.pf.module.aps.dal.mysql.load;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadPageReqVO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadRespVO;
import com.iboot.pf.module.aps.dal.dataobject.load.ApsLoadDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/** APS-资源负荷 Mapper。 */
@Mapper
public interface ApsLoadMapper extends BaseMapperX<ApsLoadDO> {

    default PageResult<LoadRespVO> selectPage(LoadPageReqVO reqVO) {
        IPage<LoadRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    LoadRespVO selectJoinById(@Param("id") Long id);

    IPage<LoadRespVO> selectJoinPage(IPage<LoadRespVO> page, @Param("req") LoadPageReqVO reqVO);

    /** 按版本列出负荷，按资源与日期排序。 */
    default List<ApsLoadDO> selectListByVersionId(Long versionId) {
        return selectList(new LambdaQueryWrapper<ApsLoadDO>()
                .eq(ApsLoadDO::getVersionId, versionId)
                .orderByAsc(ApsLoadDO::getResId, ApsLoadDO::getWorkDate));
    }

    /** 唯一索引 uk_aps_load_res_day_deleted 的应用层前置校验。 */
    default ApsLoadDO selectByVersionIdAndResIdAndWorkDate(Long versionId, Long resId, LocalDate workDate) {
        return selectOne(new LambdaQueryWrapper<ApsLoadDO>()
                .eq(ApsLoadDO::getVersionId, versionId)
                .eq(ApsLoadDO::getResId, resId)
                .eq(ApsLoadDO::getWorkDate, workDate));
    }

    /**
     * 物理删除某版本的负荷。
     *
     * <p>负荷是每次重算整体重建的派生数据，用逻辑删除会让唯一索引（含 deleted）在重算后撞车，
     * 所以这里必须物理删除。
     */
    @Delete("DELETE FROM aps_load WHERE version_id = #{versionId}")
    int deletePhysicalByVersionId(Long versionId);

}
