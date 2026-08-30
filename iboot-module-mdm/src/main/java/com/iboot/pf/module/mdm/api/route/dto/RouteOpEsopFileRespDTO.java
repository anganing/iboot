package com.iboot.pf.module.mdm.api.route.dto;

import lombok.Data;

/**
 * 工艺路线工序 ESOP 附件 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteOpEsopFileRespDTO {

    /** 附件编号 */
    private Long id;

    /** 作业步骤编号 */
    private Long esopId;

    /** 附件展示顺序 */
    private Integer seq;

    /** 文件类型：IMAGE-图片，VIDEO-视频，PDF-PDF文件，DRAWING-图纸，OFFICE-Office文档，OTHER-其他 */
    private String fileType;

    /** 原始文件名 */
    private String fileName;

    /** 文件预签名访问地址 */
    private String fileUrl;

    /** infra 文件编号 */
    private Long fileId;

}
