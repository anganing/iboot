package com.iboot.pf.module.wms.dal.mysql.devicetask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.devicetask.DeviceTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.devicetask.vo.*;

/**
 * WMS-设备搬运任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface DeviceTaskMapper extends BaseMapperX<DeviceTaskDO> {

    default PageResult<DeviceTaskRespVO> selectPage(DeviceTaskPageReqVO reqVO) {
        IPage<DeviceTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    DeviceTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<DeviceTaskRespVO> selectJoinPage(IPage<DeviceTaskRespVO> page, @Param("req") DeviceTaskPageReqVO reqVO);


    /** 唯一索引的应用层前置校验，命中说明业务编码重复。 */
    default DeviceTaskDO selectByTaskNo(String taskNo) {
        return selectOne(DeviceTaskDO::getTaskNo, taskNo);
    }

}