package com.iboot.pf.module.mdm.dal.dataobject.esop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ESOP 作业步骤附件 DO：一个作业步骤可挂多个图片/视频/图纸等附件。
 *
 * @author iboot
 */
@TableName("mdm_route_op_esop_file")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteOpEsopFileDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 作业步骤编号
     */
    private Long esopId;
    /**
     * 附件展示顺序
     */
    private Integer seq;
    /**
     * 文件类型：IMAGE-图片，VIDEO-视频，PDF-PDF文件，DRAWING-图纸，OFFICE-Office文档，OTHER-其他
     */
    private String fileType;
    /**
     * 原始文件名
     */
    private String fileName;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * infra 文件编号
     */
    private Long fileId;

}
