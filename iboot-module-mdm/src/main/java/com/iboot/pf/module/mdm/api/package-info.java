/**
 * MDM API 包，定义并实现提供给 WMS / MES / APS 的只读主数据门面。
 *
 * <p>接口只暴露 DTO，不暴露 DO、VO、Mapper、Service；批量查询均为一次 SQL，无 N+1。
 */
package com.iboot.pf.module.mdm.api;
