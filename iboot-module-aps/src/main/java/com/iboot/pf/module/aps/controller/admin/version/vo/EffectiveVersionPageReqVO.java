package com.iboot.pf.module.aps.controller.admin.version.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 曾经生效过的排程版本分页 Request VO。
 *
 * <p>面向车间与 PMC 的"排程结果"页：他们只该看到下过车间的计划，所以筛选条件只有场景编码与状态。
 * 场景编码用文本模糊匹配而不是场景选择器，选择器要打 {@code /aps/scenario/page}，
 * 那个接口挂 {@code aps:scenario:query}，为了一个筛选框给车间开排程场景的查询权不合适。
 *
 * <p>{@code status} 故意不加 {@code @InEnum}：白名单外的取值由 Service 回落成只查已发布，
 * 而不是报参数错误。校验拦截会把"能查哪些状态"这条规则暴露成一个可试探的接口。
 */
@Schema(description = "管理后台 - APS-生效排程版本分页 Request VO")
@Data
public class EffectiveVersionPageReqVO extends PageParam {

    @Schema(description = "排程场景编码", example = "SC202609001")
    private String scenarioCode;

    @Schema(description = "状态，只允许已发布与已被替换", example = "PUBLISHED")
    private String status;

}
