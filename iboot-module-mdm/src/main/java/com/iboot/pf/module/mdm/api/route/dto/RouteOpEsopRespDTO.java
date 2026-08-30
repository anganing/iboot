package com.iboot.pf.module.mdm.api.route.dto;

import lombok.Data;

import java.util.List;

/**
 * 工艺路线工序 ESOP 作业步骤 Response DTO
 *
 * @author iboot
 */
@Data
public class RouteOpEsopRespDTO {

    /** ESOP 作业步骤编号 */
    private Long id;

    /** 路线工序编号 */
    private Long routeOpId;

    /** 展示顺序 */
    private Integer seq;

    /** 标题 */
    private String title;

    /** 作业要点文本 */
    private String content;

    /** 工艺参数 JSON */
    private String paramJson;

    /** 是否要求作业者确认阅读 */
    private Boolean requiredConfirmFlag;

    /** 状态：0-启用，1-停用 */
    private Integer status;

    /** 附件列表（fileUrl 为预签名地址） */
    private List<RouteOpEsopFileRespDTO> files;

}
