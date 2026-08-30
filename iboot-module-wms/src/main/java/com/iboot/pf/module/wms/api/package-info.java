/**
 * WMS 跨模块 API 门面。
 *
 * <p>对外只暴露 DTO，不暴露 DO、Mapper、Service，也不暴露内部枚举。
 * 调用方（MES / APS / Integration / UI）通过 Spring Bean 注入使用，本包不提供 HTTP 接口。
 *
 * <p>约定：
 * <ol>
 *     <li>查询方法在实现类上加 {@code @DataPermission(enable = false)}，避免被当前用户数据权限过滤。</li>
 *     <li>命令方法在实现类上加 {@code @Validated}，入参 DTO 使用 Jakarta Validation。</li>
 *     <li>所有命令 DTO 继承 {@link com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO}，必须带幂等键。</li>
 *     <li>批量查询统一收 {@code Collection<Long>}，空集合返回空结果，不抛异常。</li>
 * </ol>
 */
package com.iboot.pf.module.wms.api;
