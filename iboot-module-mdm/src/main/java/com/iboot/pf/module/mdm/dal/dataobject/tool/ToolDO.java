package com.iboot.pf.module.mdm.dal.dataobject.tool;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工装模具刀具 DO
 *
 * @author iboot
 */
@TableName("mdm_tool")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 工装类型：FIXTURE-工装，MOLD-模具，CUTTER-刀具，GAUGE-量具
     */
    private String toolType;
    /**
     * 所属工作中心编号
     */
    private Long wcId;
    /**
     * 保有数量
     */
    private Integer qtyTotal;
    /**
     * 寿命类型：COUNT-次数，TIME-时长
     */
    private String lifeType;
    /**
     * 寿命上限
     */
    private Integer lifeLimit;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
