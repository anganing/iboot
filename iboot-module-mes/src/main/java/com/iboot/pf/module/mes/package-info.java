/**
 * mes 包下，制造执行系统（Manufacturing Execution System）。
 * 例如说：生产工单、工艺路线、报工、设备、质量等等
 *
 * 1. Controller URL：以 /mes/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 mes_ 开头，方便在数据库中区分
 *
 * 注意，由于 Mes 模块下容易和其它模块重名，所以类名都加 Mes 前缀~
 */
package com.iboot.pf.module.mes;
