/** ========== COMMON - 通用模块 ========== */
const COMMON_DICT = {
  USER_TYPE: 'user_type',
  COMMON_STATUS: 'common_status',
  TERMINAL: 'terminal', // 终端
  DATE_INTERVAL: 'date_interval', // 数据间隔
} as const;

/** ========== SYSTEM - 系统模块 ========== */
const SYSTEM_DICT = {
  SYSTEM_USER_SEX: 'system_user_sex',
  SYSTEM_MENU_TYPE: 'system_menu_type',
  SYSTEM_ROLE_TYPE: 'system_role_type',
  SYSTEM_DATA_SCOPE: 'system_data_scope',
  SYSTEM_NOTICE_TYPE: 'system_notice_type',
  SYSTEM_LOGIN_TYPE: 'system_login_type',
  SYSTEM_LOGIN_RESULT: 'system_login_result',
  SYSTEM_SMS_CHANNEL_CODE: 'system_sms_channel_code',
  SYSTEM_SMS_TEMPLATE_TYPE: 'system_sms_template_type',
  SYSTEM_SMS_SEND_STATUS: 'system_sms_send_status',
  SYSTEM_SMS_RECEIVE_STATUS: 'system_sms_receive_status',
  SYSTEM_OAUTH2_GRANT_TYPE: 'system_oauth2_grant_type',
  SYSTEM_MAIL_SEND_STATUS: 'system_mail_send_status',
  SYSTEM_NOTIFY_TEMPLATE_TYPE: 'system_notify_template_type',
  SYSTEM_SOCIAL_TYPE: 'system_social_type',
} as const;

/** ========== INFRA - 基础设施模块 ========== */
const INFRA_DICT = {
  INFRA_BOOLEAN_STRING: 'infra_boolean_string',
  INFRA_JOB_STATUS: 'infra_job_status',
  INFRA_JOB_LOG_STATUS: 'infra_job_log_status',
  INFRA_API_ERROR_LOG_PROCESS_STATUS: 'infra_api_error_log_process_status',
  INFRA_CONFIG_TYPE: 'infra_config_type',
  INFRA_CODEGEN_TEMPLATE_TYPE: 'infra_codegen_template_type',
  INFRA_CODEGEN_FRONT_TYPE: 'infra_codegen_front_type',
  INFRA_CODEGEN_SCENE: 'infra_codegen_scene',
  INFRA_FILE_STORAGE: 'infra_file_storage',
  INFRA_OPERATE_TYPE: 'infra_operate_type',
} as const;

/** ========== BPM - 工作流模块 ========== */
const BPM_DICT = {
  BPM_MODEL_FORM_TYPE: 'bpm_model_form_type', // BPM 模型表单类型
  BPM_MODEL_TYPE: 'bpm_model_type', // BPM 模型类型
  BPM_OA_LEAVE_TYPE: 'bpm_oa_leave_type', // BPM OA 请假类型
  BPM_PROCESS_INSTANCE_STATUS: 'bpm_process_instance_status', // BPM 流程实例状态
  BPM_PROCESS_LISTENER_TYPE: 'bpm_process_listener_type', // BPM 流程监听器类型
  BPM_PROCESS_LISTENER_VALUE_TYPE: 'bpm_process_listener_value_type', // BPM 流程监听器值类型
  BPM_COMMENT_TYPE: 'bpm_comment_type', // BPM 评论类型
  BPM_TASK_CANDIDATE_STRATEGY: 'bpm_task_candidate_strategy', // BPM 任务候选人策略
  BPM_TASK_STATUS: 'bpm_task_status', // BPM 任务状态
} as const;

/** ========== PAY - 支付模块 ========== */
const PAY_DICT = {
  PAY_CHANNEL_CODE: 'pay_channel_code', // 支付渠道编码类型
  PAY_ORDER_STATUS: 'pay_order_status', // 商户支付订单状态
  PAY_REFUND_STATUS: 'pay_refund_status', // 退款订单状态
  PAY_NOTIFY_STATUS: 'pay_notify_status', // 商户支付回调状态
  PAY_NOTIFY_TYPE: 'pay_notify_type', // 商户支付回调状态
  PAY_TRANSFER_STATUS: 'pay_transfer_status', // 转账订单状态
  PAY_TRANSFER_TYPE: 'pay_transfer_type', // 转账类型
} as const;

/** ========== MP - 公众号模块 ========== */
const MP_DICT = {
  MP_AUTO_REPLY_REQUEST_MATCH: 'mp_auto_reply_request_match', // 自动回复请求匹配类型
  MP_MESSAGE_TYPE: 'mp_message_type', // 消息类型
} as const;

/** ========== MEMBER - 会员模块 ========== */
const MEMBER_DICT = {
  MEMBER_EXPERIENCE_BIZ_TYPE: 'member_experience_biz_type', // 会员经验业务类型
  MEMBER_POINT_BIZ_TYPE: 'member_point_biz_type', // 积分的业务类型
} as const;

/** ========== MALL - 商城模块 ========== */
const MALL_DICT = {
  /** ========== MALL - 商品模块 ========== */
  PRODUCT_SPU_STATUS: 'product_spu_status', // 商品状态

  /** ========== MALL - 交易模块 ========== */
  EXPRESS_CHARGE_MODE: 'trade_delivery_express_charge_mode', // 快递的计费方式
  TRADE_AFTER_SALE_STATUS: 'trade_after_sale_status', // 售后 - 状态
  TRADE_AFTER_SALE_TYPE: 'trade_after_sale_type', // 售后 - 类型
  TRADE_AFTER_SALE_WAY: 'trade_after_sale_way', // 售后 - 方式
  TRADE_DELIVERY_TYPE: 'trade_delivery_type', // 配送方式
  TRADE_ORDER_ITEM_AFTER_SALE_STATUS: 'trade_order_item_after_sale_status', // 订单项 - 售后状态
  TRADE_ORDER_STATUS: 'trade_order_status', // 订单 - 状态
  TRADE_ORDER_TYPE: 'trade_order_type', // 订单 - 类型
  BROKERAGE_BANK_NAME: 'brokerage_bank_name', // 佣金提现银行
  BROKERAGE_BIND_MODE: 'brokerage_bind_mode', // 分销关系绑定模式
  BROKERAGE_ENABLED_CONDITION: 'brokerage_enabled_condition', // 分佣模式
  BROKERAGE_RECORD_BIZ_TYPE: 'brokerage_record_biz_type', // 佣金业务类型
  BROKERAGE_RECORD_STATUS: 'brokerage_record_status', // 佣金状态
  BROKERAGE_WITHDRAW_STATUS: 'brokerage_withdraw_status', // 佣金提现状态
  BROKERAGE_WITHDRAW_TYPE: 'brokerage_withdraw_type', // 佣金提现类型

  /** ========== MALL - 营销模块 ========== */

  PROMOTION_BANNER_POSITION: 'promotion_banner_position', // banner 定位
  PROMOTION_BARGAIN_RECORD_STATUS: 'promotion_bargain_record_status', // 砍价记录的状态
  PROMOTION_COMBINATION_RECORD_STATUS: 'promotion_combination_record_status', // 拼团记录的状态
  PROMOTION_CONDITION_TYPE: 'promotion_condition_type', // 营销的条件类型枚举
  PROMOTION_COUPON_STATUS: 'promotion_coupon_status', // 优惠劵的状态
  PROMOTION_COUPON_TAKE_TYPE: 'promotion_coupon_take_type', // 优惠劵的领取方式
  PROMOTION_COUPON_TEMPLATE_VALIDITY_TYPE:
    'promotion_coupon_template_validity_type', // 优惠劵模板的有限期类型
  PROMOTION_DISCOUNT_TYPE: 'promotion_discount_type', // 优惠类型
  PROMOTION_PRODUCT_SCOPE: 'promotion_product_scope', // 营销的商品范围
} as const;

/** ========== CRM - 客户管理模块 ========== */
const CRM_DICT = {
  CRM_AUDIT_STATUS: 'crm_audit_status', // CRM 审批状态
  CRM_BIZ_TYPE: 'crm_biz_type', // CRM 业务类型
  CRM_BUSINESS_END_STATUS_TYPE: 'crm_business_end_status_type', // CRM 商机结束状态类型
  CRM_CUSTOMER_INDUSTRY: 'crm_customer_industry', // CRM 客户所属行业
  CRM_CUSTOMER_LEVEL: 'crm_customer_level', // CRM 客户级别
  CRM_CUSTOMER_SOURCE: 'crm_customer_source', // CRM 客户来源
  CRM_FOLLOW_UP_TYPE: 'crm_follow_up_type', // CRM 跟进方式
  CRM_PERMISSION_LEVEL: 'crm_permission_level', // CRM 数据权限的级别
  CRM_PRODUCT_STATUS: 'crm_product_status', // CRM 商品状态
  CRM_PRODUCT_UNIT: 'crm_product_unit', // CRM 产品单位
  CRM_RECEIVABLE_RETURN_TYPE: 'crm_receivable_return_type', // CRM 回款的还款方式
} as const;

/** ========== ERP - 企业资源计划模块 ========== */
const ERP_DICT = {
  ERP_AUDIT_STATUS: 'erp_audit_status', // ERP 审批状态
  ERP_STOCK_RECORD_BIZ_TYPE: 'erp_stock_record_biz_type', // 库存明细的业务类型
} as const;

/** ========== FMS - 财务管理模块 ========== */
const FMS_DICT = {
  FMS_ACCOUNT_USER_LEVEL: 'fms_account_user_level', // FMS 账套用户权限级别
  FMS_SUBJECT_CATEGORY: 'fms_subject_category', // FMS 科目类别
  FMS_DEBIT_CREDIT_DIRECTION: 'fms_debit_credit_direction', // FMS 借贷方向
  FMS_FINANCE_INDICATOR_TYPE: 'fms_finance_indicator_type', // FMS 财务指标取数报表类型
  FMS_SUBJECT_TYPE: 'fms_subject_type', // FMS 科目类型
  FMS_AUXILIARY_TYPE: 'fms_auxiliary_type', // FMS 辅助核算类别
  FMS_VOUCHER_STATUS: 'fms_voucher_status', // FMS 凭证状态
  FMS_VOUCHER_TIDY_TYPE: 'fms_voucher_tidy_type', // FMS 凭证整理方式
  FMS_FORMULA_RULE: 'fms_formula_rule', // FMS 报表公式取数规则
  FMS_REPORT_TYPE: 'fms_report_type', // FMS 财务报表类型
  FMS_REPORT_PERIOD_TYPE: 'fms_report_period_type', // FMS 财务报表期间类型
  FMS_LEDGER_BALANCE_MODE: 'fms_ledger_balance_mode', // FMS 账簿余额方向模式
  FMS_ACCOUNTING_STANDARD: 'fms_accounting_standard', // FMS 会计制度
  FMS_CLOSING_TYPE: 'fms_closing_type', // FMS 结账方案类型
  FMS_CLOSING_TIME_TYPE: 'fms_closing_time_type', // FMS 结账取数期间
  FMS_CLOSING_VOUCHER_TYPE: 'fms_closing_voucher_type', // FMS 结账凭证类型
  FMS_CLOSING_TEMPLATE_CATEGORY: 'fms_closing_template_category', // FMS 结账模板分类
} as const;

/** ========== AI - 人工智能模块 ========== */
const AI_DICT = {
  AI_GENERATE_MODE: 'ai_generate_mode', // AI 生成模式
  AI_IMAGE_STATUS: 'ai_image_status', // AI 图片状态
  AI_MODEL_TYPE: 'ai_model_type', // AI 模型类型
  AI_MUSIC_STATUS: 'ai_music_status', // AI 音乐状态
  AI_PLATFORM: 'ai_platform', // AI 平台
  AI_WRITE_FORMAT: 'ai_write_format', // AI 写作格式
  AI_WRITE_LANGUAGE: 'ai_write_language', // AI 写作语言
  AI_WRITE_LENGTH: 'ai_write_length', // AI 写作长度
  AI_WRITE_TONE: 'ai_write_tone', // AI 写作语气
  AI_WRITE_TYPE: 'ai_write_type', // AI 写作类型
  AI_MCP_CLIENT_NAME: 'ai_mcp_client_name', // AI MCP Client 名字
} as const;

/** ========== IOT - 物联网模块 ========== */
const IOT_DICT = {
  IOT_ALERT_LEVEL: 'iot_alert_level', // IoT 告警级别
  IOT_ALERT_RECEIVE_TYPE: 'iot_alert_receive_type', // IoT 告警接收类型
  IOT_SERIALIZE_TYPE: 'iot_serialize_type', // IOT 序列化类型
  IOT_DATA_FORMAT: 'iot_data_format', // IOT 数据格式
  IOT_DATA_SINK_TYPE_ENUM: 'iot_data_sink_type_enum', // IoT 数据流转目的类型
  IOT_DATA_TYPE: 'iot_data_type', // IOT 数据类型
  IOT_DEVICE_STATE: 'iot_device_state', // IOT 设备状态
  IOT_LOCATION_TYPE: 'iot_location_type', // IOT 定位类型
  IOT_NET_TYPE: 'iot_net_type', // IOT 联网方式
  IOT_OTA_TASK_DEVICE_SCOPE: 'iot_ota_task_device_scope', // IoT OTA任务设备范围
  IOT_OTA_TASK_RECORD_STATUS: 'iot_ota_task_record_status', // IoT OTA 记录状态
  IOT_OTA_TASK_STATUS: 'iot_ota_task_status', // IoT OTA 任务状态
  IOT_PRODUCT_DEVICE_TYPE: 'iot_product_device_type', // IOT 产品设备类型
  IOT_PRODUCT_FUNCTION_TYPE: 'iot_product_function_type', // IOT 产品功能类型
  IOT_PRODUCT_STATUS: 'iot_product_status', // IOT 产品状态
  IOT_PROTOCOL_TYPE: 'iot_protocol_type', // IOT 接入网关协议
  IOT_RULE_SCENE_ACTION_TYPE_ENUM: 'iot_rule_scene_action_type_enum', // IoT 规则场景的触发类型枚举
  IOT_RULE_SCENE_TRIGGER_TYPE_ENUM: 'iot_rule_scene_trigger_type_enum', // IoT 场景流转的触发类型枚举
  IOT_RW_TYPE: 'iot_rw_type', // IOT 读写类型
  IOT_THING_MODEL_TYPE: 'iot_thing_model_type', // IOT 产品功能类型
  IOT_THING_MODEL_UNIT: 'iot_thing_model_unit', // IOT 物模型单位
  IOT_UNIT_TYPE: 'iot_unit_type', // IOT 单位类型
  IOT_VALIDATE_TYPE: 'iot_validate_type', // IOT 数据校验级别
  IOT_MODBUS_MODE: 'iot_modbus_mode', // IoT Modbus 工作模式
  IOT_MODBUS_FRAME_FORMAT: 'iot_modbus_frame_format', // IoT Modbus 帧格式
} as const;

/** ========== HRM - 人力资源模块 ========== */
const HRM_DICT = {
  HRM_EMPLOYEE_ENTRY_STATUS: 'hrm_employee_entry_status', // HRM 员工入职状态
  HRM_EMPLOYEE_STATUS: 'hrm_employee_status', // HRM 员工状态
  HRM_EMPLOYEE_TYPE: 'hrm_employee_type', // HRM 聘用形式
  HRM_EMPLOYEE_EDUCATION: 'hrm_employee_education', // HRM 员工学历
  HRM_SALARY_CHANGE_REASON: 'hrm_salary_change_reason', // HRM 薪资调整原因
  HRM_SALARY_CHANGE_TYPE: 'hrm_salary_change_type', // HRM 薪资档案状态
  HRM_SALARY_CHANGE_RECORD_STATUS: 'hrm_salary_change_record_status', // HRM 调薪记录状态
  HRM_SALARY_SLIP_READ_STATUS: 'hrm_salary_slip_read_status', // HRM 工资条查看状态
  HRM_SALARY_TAX_TYPE: 'hrm_salary_tax_type', // HRM 薪资计税类型
  HRM_SALARY_YES_NO: 'hrm_salary_yes_no', // HRM 薪资是否
  HRM_SALARY_OPTION_TYPE: 'hrm_salary_option_type', // HRM 薪资项加减类型
  HRM_INSURANCE_PROJECT_TYPE: 'hrm_insurance_project_type', // HRM 社保项目类型
  HRM_INSURANCE_SCHEME_TYPE: 'hrm_insurance_scheme_type', // HRM 社保方案类型
  HRM_INSURANCE_EMP_STATUS: 'hrm_insurance_emp_status', // HRM 员工参保状态
  HRM_ATTENDANCE_HOLIDAY_TYPE: 'hrm_attendance_holiday_type', // HRM 考勤节假日类型
  HRM_RECRUIT_CANDIDATE_STATUS: 'hrm_recruit_candidate_status', // HRM 招聘候选人状态
  HRM_RECRUIT_CANDIDATE_EDUCATION: 'hrm_recruit_candidate_education', // HRM 招聘候选人学历
  HRM_RECRUIT_INTERVIEW_TYPE: 'hrm_recruit_interview_type', // HRM 招聘面试方式
  HRM_RECRUIT_INTERVIEW_RESULT: 'hrm_recruit_interview_result', // HRM 招聘面试结果
  HRM_RECRUIT_POST_STATUS: 'hrm_recruit_post_status', // HRM 招聘职位状态
  HRM_RECRUIT_JOB_NATURE: 'hrm_recruit_job_nature', // HRM 招聘工作性质
  HRM_RECRUIT_WORK_TIME: 'hrm_recruit_work_time', // HRM 招聘工作经验
  HRM_RECRUIT_POST_EDUCATION: 'hrm_recruit_post_education', // HRM 招聘职位学历要求
  HRM_RECRUIT_SALARY_UNIT: 'hrm_recruit_salary_unit', // HRM 招聘薪资单位
  HRM_RECRUIT_EMERGENCY_LEVEL: 'hrm_recruit_emergency_level', // HRM 招聘紧急程度
  HRM_ATTENDANCE_CLOCK_TYPE: 'hrm_attendance_clock_type', // HRM 打卡类型
  HRM_ATTENDANCE_CLOCK_SOURCE: 'hrm_attendance_clock_source', // HRM 打卡来源
  HRM_ATTENDANCE_CLOCK_STATUS: 'hrm_attendance_clock_status', // HRM 打卡状态
  HRM_ATTENDANCE_LEAVE_TYPE: 'hrm_attendance_leave_type', // HRM 请假类型
  HRM_ATTENDANCE_YES_NO: 'hrm_attendance_yes_no', // HRM 考勤是否
  HRM_ATTENDANCE_LATE_EARLY_DEDUCT_METHOD:
    'hrm_attendance_late_early_deduct_method', // HRM 迟到早退扣款方式
  HRM_ATTENDANCE_ABSENTEEISM_DEDUCT_METHOD:
    'hrm_attendance_absenteeism_deduct_method', // HRM 旷工扣款方式
  HRM_ATTENDANCE_MISSCARD_DEDUCT_METHOD:
    'hrm_attendance_misscard_deduct_method', // HRM 缺卡扣款方式
  HRM_PERFORMANCE_PLAN_STATUS: 'hrm_performance_plan_status', // HRM 绩效计划状态
  HRM_PERFORMANCE_STAGE_STATUS: 'hrm_performance_stage_status', // HRM 绩效阶段状态
  HRM_PERFORMANCE_ASSESSMENT_STAGE_STATUS:
    'hrm_performance_assessment_stage_status', // HRM 绩效考核阶段处理状态
  HRM_PERFORMANCE_APPEAL_STATUS: 'hrm_performance_appeal_status', // HRM 绩效申诉状态
  HRM_PERFORMANCE_SCORE_CALCULATION: 'hrm_performance_score_calculation', // HRM 绩效计分方式
  HRM_PERFORMANCE_UPPER_LIMIT_TYPE: 'hrm_performance_upper_limit_type', // HRM 绩效上限类型
  HRM_PERFORMANCE_YES_NO: 'hrm_performance_yes_no', // HRM 绩效是否
} as const;

/** ========== IM - 即时通讯模块 ========== */
const IM_DICT = {
  IM_CHANNEL_MATERIAL_TYPE: 'im_channel_material_type', // IM 频道素材类型
  IM_CONTENT_TYPE: 'im_content_type', // IM 消息内容类型
  IM_FRIEND_ADD_SOURCE: 'im_friend_add_source', // IM 好友添加来源
  IM_FRIEND_REQUEST_HANDLE_RESULT: 'im_friend_request_handle_result', // IM 好友申请处理结果
  IM_FRIEND_STATUS: 'im_friend_status', // IM 好友状态
  IM_GROUP_ADD_SOURCE: 'im_group_add_source', // IM 加群来源
  IM_GROUP_MEMBER_ROLE: 'im_group_member_role', // IM 群成员角色
  IM_GROUP_REQUEST_HANDLE_RESULT: 'im_group_request_handle_result', // IM 加群申请处理结果
  IM_GROUP_STATUS: 'im_group_status', // IM 群状态
  IM_MESSAGE_RECEIPT_STATUS: 'im_message_receipt_status', // IM 消息回执状态（私聊 / 群聊共用）
  IM_MESSAGE_STATUS: 'im_message_status', // IM 消息状态（私聊 / 群聊共用）
  IM_RTC_CALL_CONVERSATION_TYPE: 'im_rtc_call_conversation_type', // IM 通话会话类型
  IM_RTC_CALL_END_REASON: 'im_rtc_call_end_reason', // IM 通话结束原因
  IM_RTC_CALL_MEDIA_TYPE: 'im_rtc_call_media_type', // IM 通话媒体类型
  IM_RTC_CALL_STATUS: 'im_rtc_call_status', // IM 通话状态
  IM_RTC_PARTICIPANT_ROLE: 'im_rtc_participant_role', // IM 通话参与者角色
  IM_RTC_PARTICIPANT_STATUS: 'im_rtc_participant_status', // IM 通话参与者状态
} as const;

/** ========== MES - 制造执行模块 ========== */
const MES_DICT = {
  // ---------- 制造执行核心域（生产需求、工单、工序任务、质检、在制） ----------
  MES_DEMAND_SOURCE_TYPE: 'mes_demand_source_type', // MES 生产需求来源类型
  MES_DEMAND_STATUS: 'mes_demand_status', // MES 生产需求状态
  MES_PROD_ORDER_STATUS: 'mes_prod_order_status', // MES 生产工单状态
  MES_WORK_ORDER_STATUS: 'mes_work_order_status', // MES 作业工单状态
  MES_OP_TASK_STATUS: 'mes_op_task_status', // MES 工序任务状态
  MES_OP_EXEC_MODE: 'mes_op_exec_mode', // MES 工序执行模式
  MES_QC_MODE: 'mes_qc_mode', // MES 检验模式
  MES_SKIP_POLICY: 'mes_skip_policy', // MES 跳序策略
  MES_KIT_STATUS: 'mes_kit_status', // MES 齐套状态
  MES_KIT_CHECK_SCOPE: 'mes_kit_check_scope', // MES 齐套检查范围
  MES_MATERIAL_REQ_TYPE: 'mes_material_req_type', // MES 领退补料申请类型
  MES_MATERIAL_REQ_STATUS: 'mes_material_req_status', // MES 领退补料申请状态
  MES_MATERIAL_REQ_LINE_STATUS: 'mes_material_req_line_status', // MES 领退补料明细状态
  MES_PROD_REPORT_STATUS: 'mes_prod_report_status', // MES 生产报工状态
  MES_REWORK_STATUS: 'mes_rework_status', // MES 返工任务状态
  MES_QC_TRIGGER_POINT: 'mes_qc_trigger_point', // MES 质检触发点
  MES_QC_OBJECT_TYPE: 'mes_qc_object_type', // MES 检验对象类型
  MES_QC_TASK_STATUS: 'mes_qc_task_status', // MES 质检任务状态
  MES_QC_CONCLUSION: 'mes_qc_conclusion', // MES 质检结论
  MES_QC_ITEM_CONCLUSION: 'mes_qc_item_conclusion', // MES 单项检验结论
  MES_QC_ITEM_TYPE: 'mes_qc_item_type', // MES 检验项类型
  MES_QC_PLAN_STATUS: 'mes_qc_plan_status', // MES 检验方案状态
  MES_QC_SAMPLE_TYPE: 'mes_qc_sample_type', // MES 抽样类型
  MES_DISPOSITION_TYPE: 'mes_disposition_type', // MES 不合格品处置类型
  MES_DISPOSITION_STATUS: 'mes_disposition_status', // MES 不合格品处置单状态
  MES_DISPATCH_TASK_STATUS: 'mes_dispatch_task_status', // MES 派工任务状态
  MES_SKIP_APPROVE_STATUS: 'mes_skip_approve_status', // MES 跳序审批状态
  MES_SKIP_RECORD_STATUS: 'mes_skip_record_status', // MES 跳过记录状态
  MES_SKIP_REASON_TYPE: 'mes_skip_reason_type', // MES 跳序原因类型
  MES_RGV_TASK_TYPE: 'mes_rgv_task_type', // MES RGV 任务类型
  MES_RGV_DISPATCH_STATUS: 'mes_rgv_dispatch_status', // MES RGV 下发状态
  MES_RGV_TASK_STATUS: 'mes_rgv_task_status', // MES RGV 执行状态
  MES_WIP_TXN_TYPE: 'mes_wip_txn_type', // MES 在制事务类型
  MES_BIZ_TYPE: 'mes_biz_type', // MES 业务单据类型
  // ---------- 历史遗留字典 ----------
  MES_MD_ITEM_OR_PRODUCT: 'mes_md_item_or_product', // MES 物料/产品标识
  MES_MD_AUTO_CODE_CYCLE_METHOD: 'mes_md_auto_code_cycle_method', // MES 编码规则循环方式
  MES_MD_AUTO_CODE_PADDED_METHOD: 'mes_md_auto_code_padded_method', // MES 编码规则补齐方式
  MES_MD_AUTO_CODE_PART_TYPE: 'mes_md_auto_code_part_type', // MES 编码规则分段类型
  MES_CLIENT_TYPE: 'mes_client_type', // MES 客户类型
  MES_VENDOR_LEVEL: 'mes_vendor_level', // MES 供应商级别
  MES_CAL_HOLIDAY_TYPE: 'mes_cal_holiday_type', // MES 假期类型
  MES_CAL_SHIFT_TYPE: 'mes_cal_shift_type', // MES 轮班方式
  MES_CAL_SHIFT_METHOD: 'mes_cal_shift_method', // MES 倒班方式
  MES_CAL_CALENDAR_TYPE: 'mes_cal_calendar_type', // MES 班组类型
  MES_CAL_PLAN_STATUS: 'mes_cal_plan_status', // MES 排班计划状态
  MES_TM_TOOL_STATUS: 'mes_tm_tool_status', // MES 工具状态
  MES_TM_MAINTEN_TYPE: 'mes_tm_mainten_type', // MES 保养维护类型
  MES_DV_MACHINERY_STATUS: 'mes_dv_machinery_status', // MES 设备状态
  MES_DV_SUBJECT_TYPE: 'mes_dv_subject_type', // MES 点检保养项目类型
  MES_DV_CYCLE_TYPE: 'mes_dv_cycle_type', // MES 点检保养周期类型
  MES_DV_CHECK_PLAN_STATUS: 'mes_dv_check_plan_status', // MES 点检保养方案状态
  MES_MAINTEN_RECORD_STATUS: 'mes_mainten_record_status', // MES 保养记录状态
  MES_MAINTEN_STATUS: 'mes_mainten_status', // MES 保养结果
  MES_DV_REPAIR_STATUS: 'mes_dv_repair_status', // MES 维修工单状态
  MES_DV_REPAIR_RESULT: 'mes_dv_repair_result', // MES 维修结果
  MES_DV_CHECK_RECORD_STATUS: 'mes_dv_check_record_status', // MES 点检记录状态
  MES_DV_CHECK_RESULT: 'mes_dv_check_result', // MES 点检结果
  MES_PRO_LINK_TYPE: 'mes_pro_link_type', // MES 工序关系类型
  MES_PRO_WORK_ORDER_STATUS: 'mes_pro_work_order_status', // MES 生产工单状态
  MES_PRO_WORK_ORDER_TYPE: 'mes_pro_work_order_type', // MES 工单类型
  MES_PRO_WORK_ORDER_SOURCE_TYPE: 'mes_pro_work_order_source_type', // MES 工单来源类型
  MES_PRO_TASK_STATUS: 'mes_pro_task_status', // MES 生产任务状态
  MES_PRO_FEEDBACK_STATUS: 'mes_pro_feedback_status', // MES 生产报工状态
  MES_PRO_FEEDBACK_TYPE: 'mes_pro_feedback_type', // MES 生产报工类型
  MES_PRO_FEEDBACK_CHANNEL: 'mes_pro_feedback_channel', // MES 生产报工途径
  MES_PRO_ANDON_STATUS: 'mes_pro_andon_status', // MES 安灯处置状态
  MES_PRO_ANDON_LEVEL: 'mes_pro_andon_level', // MES 安灯级别
  MES_PRO_WORK_RECORD_TYPE: 'mes_pro_work_record_type', // MES 上下工状态类型
  MES_TIME_UNIT_TYPE: 'mes_time_unit_type', // MES 时间单位
  MES_ORDER_STATUS: 'mes_order_status', // MES 单据状态
  MES_INDICATOR_TYPE: 'mes_indicator_type', // MES 检测项类型
  MES_QC_RESULT_TYPE: 'mes_qc_result_type', // MES 质检结果值类型
  MES_QC_TYPE: 'mes_qc_type', // MES 质检方案类型
  MES_QC_CHECK_RESULT: 'mes_qc_check_result', // MES 检测结果
  MES_QC_SOURCE_DOC_TYPE: 'mes_qc_source_doc_type', // MES 来源单据类型
  MES_IPQC_TYPE: 'mes_ipqc_type', // MES IPQC 检验类型
  MES_RQC_TYPE: 'mes_rqc_type', // MES 退货检验类型
  MES_DEFECT_LEVEL: 'mes_defect_level', // MES 缺陷等级
  MES_WM_BARCODE_BIZ_TYPE: 'mes_wm_barcode_biz_type', // MES 条码业务类型
  MES_WM_BARCODE_FORMAT: 'mes_wm_barcode_format', // MES 条码格式
  MES_WM_PACKAGE_STATUS: 'mes_wm_package_status', // MES 装箱单状态
  MES_WM_PRODUCT_SALES_STATUS: 'mes_wm_product_sales_status', // MES 销售出库单状态
  MES_WM_QUALITY_STATUS: 'mes_wm_quality_status', // MES 质量状态
  MES_WM_MISC_ISSUE_TYPE: 'mes_wm_misc_issue_type', // MES 杂项出库类型
  MES_WM_MISC_ISSUE_STATUS: 'mes_wm_misc_issue_status', // MES 杂项出库单状态
  MES_WM_MISC_RECEIPT_TYPE: 'mes_wm_misc_receipt_type', // MES 杂项入库类型
  MES_WM_MISC_RECEIPT_STATUS: 'mes_wm_misc_receipt_status', // MES 杂项入库单状态
  MES_WM_OUTSOURCE_ISSUE_STATUS: 'mes_wm_outsource_issue_status', // MES 外协发料单状态
  MES_WM_OUTSOURCE_RECEIPT_STATUS: 'mes_wm_outsource_receipt_status', // MES 外协入库单状态
  MES_WM_ARRIVAL_NOTICE_STATUS: 'mes_wm_arrival_notice_status', // MES 到货通知单状态
  MES_WM_ITEM_RECEIPT_STATUS: 'mes_wm_item_receipt_status', // MES 采购入库单状态
  MES_WM_RETURN_VENDOR_STATUS: 'mes_wm_return_vendor_status', // MES 供应商退货单状态
  MES_WM_SALES_NOTICE_STATUS: 'mes_wm_sales_notice_status', // MES 发货通知单状态
  MES_WM_RETURN_SALES_STATUS: 'mes_wm_return_sales_status', // MES 销售退货单状态
  MES_WM_RETURN_ISSUE_STATUS: 'mes_wm_return_issue_status', // MES 生产退料单状态
  MES_WM_RETURN_ISSUE_TYPE: 'mes_wm_return_issue_type', // MES 退料类型
  MES_WM_PRODUCT_ISSUE_STATUS: 'mes_wm_product_issue_status', // MES 领料出库单状态
  MES_WM_PRODUCT_RECEIPT_STATUS: 'mes_wm_product_receipt_status', // MES 产品入库单状态
  MES_WM_STOCK_TAKING_TYPE: 'mes_wm_stock_taking_type', // MES 盘点类型
  MES_WM_STOCK_TAKING_TASK_STATUS: 'mes_wm_stock_taking_task_status', // MES 盘点任务状态
  MES_WM_STOCK_TAKING_LINE_STATUS: 'mes_wm_stock_taking_task_line_status', // MES 盘点任务行状态
  MES_WM_STOCK_TAKING_PLAN_PARAM_TYPE: 'mes_wm_stock_taking_plan_param_type', // MES 盘点方案参数类型
  MES_WM_TRANSFER_STATUS: 'mes_wm_transfer_status', // MES 转移单状态
  MES_WM_TRANSFER_TYPE: 'mes_wm_transfer_type', // MES 转移单类型
} as const;

/** ========== WMS - 仓储管理模块 ========== */
const WMS_DICT = {
  WMS_ARRIVAL_LINE_STATUS: 'wms_arrival_line_status', // WMS 到货明细状态
  WMS_ARRIVAL_SOURCE_TYPE: 'wms_arrival_source_type', // WMS 到货来源类型
  WMS_ARRIVAL_STATUS: 'wms_arrival_status', // WMS 到货状态
  WMS_CONTAINER_STATUS: 'wms_container_status', // WMS 容器状态
  WMS_COUNT_LINE_STATUS: 'wms_count_line_status', // WMS 盘点明细状态
  WMS_COUNT_TYPE: 'wms_count_type', // WMS 盘点类型
  WMS_DEVICE_TASK_STATUS: 'wms_device_task_status', // WMS 设备任务状态
  WMS_DEVICE_TASK_TYPE: 'wms_device_task_type', // WMS 设备任务类型
  WMS_DEVICE_TYPE: 'wms_device_type', // WMS 设备类型
  WMS_DIMENSION_CHECK_MODE: 'wms_dimension_check_mode', // WMS 尺寸校验模式
  WMS_DISPATCH_STATUS: 'wms_dispatch_status', // WMS 任务下发状态
  WMS_HOOK_MODE: 'wms_hook_mode', // WMS 设备触发方式
  WMS_IN_BIZ_TYPE: 'wms_in_biz_type', // WMS 入库业务类型
  WMS_IO_TYPE: 'wms_io_type', // WMS 收发方向
  WMS_IQC_MODE: 'wms_iqc_mode', // WMS 来料检验模式
  WMS_LOC_STATUS: 'wms_loc_status', // WMS 库位状态
  WMS_LOC_TYPE: 'wms_loc_type', // WMS 库位类型
  WMS_LOCK_STATUS: 'wms_lock_status', // WMS 冻结状态
  WMS_MANAGED_BY: 'wms_managed_by', // WMS 仓库管理方式
  WMS_ORDER_LINE_STATUS: 'wms_order_line_status', // WMS 单据明细状态
  WMS_ORDER_STATUS: 'wms_order_status', // WMS 单据状态
  WMS_ORIGIN_TYPE: 'wms_origin_type', // WMS 原始来源类型
  WMS_OUT_BIZ_TYPE: 'wms_out_biz_type', // WMS 出库业务类型
  WMS_POST_STATUS: 'wms_post_status', // WMS 库存过账状态
  WMS_QUALITY_STATUS: 'wms_quality_status', // WMS 质量状态
  WMS_RESERVATION_STATUS: 'wms_reservation_status', // WMS 预留状态
  WMS_SOURCE_BIZ_TYPE: 'wms_source_biz_type', // WMS 来源业务类型
  WMS_SOURCE_MODULE: 'wms_source_module', // WMS 来源模块
  WMS_STOCK_SCOPE: 'wms_stock_scope', // WMS 库存范围
  WMS_STRATEGY_STATUS: 'wms_strategy_status', // WMS 策略状态
  WMS_STRATEGY_TYPE: 'wms_strategy_type', // WMS 策略类型
  WMS_TRANSFER_TYPE: 'wms_transfer_type', // WMS 转移类型
  WMS_TXN_TYPE: 'wms_txn_type', // WMS 库存事务类型
  WMS_WAVE_STATUS: 'wms_wave_status', // WMS 波次状态
  WMS_WAVE_TYPE: 'wms_wave_type', // WMS 波次类型
  WMS_WH_TYPE: 'wms_wh_type', // WMS 仓库类型
  WMS_ZONE_TYPE: 'wms_zone_type', // WMS 库区类型
} as const;

/** ========== APS - 高级计划排程模块 ========== */
const APS_DICT = {
  APS_PROJECTION_SOURCE_TYPE: 'aps_projection_source_type', // APS 工单投影来源类型
  APS_SCENARIO_STATUS: 'aps_scenario_status', // APS 排程场景状态
  APS_SHORTAGE_STATUS: 'aps_shortage_status', // APS 缺料状态
  APS_SNAPSHOT_TYPE: 'aps_snapshot_type', // APS 求解快照类型
  APS_STRATEGY_STATUS: 'aps_strategy_status', // APS 排程策略状态
  APS_TASK_DEP_TYPE: 'aps_task_dep_type', // APS 任务依赖类型
  APS_TASK_LOCK_STATUS: 'aps_task_lock_status', // APS 任务锁定状态
  APS_TASK_LOCK_TYPE: 'aps_task_lock_type', // APS 任务锁定类型
  APS_TASK_STATUS: 'aps_task_status', // APS 排程任务状态
  APS_VERSION_STATUS: 'aps_version_status', // APS 排程版本状态
} as const;

/** ========== MDM - 主数据模块 ========== */
const MDM_DICT = {
  MDM_MAT_TYPE: 'mdm_mat_type', // MDM 物料类型
  MDM_RES_TYPE: 'mdm_res_type', // MDM 资源类型
} as const;

/** 字典类型枚举 - 统一导出 */
const DICT_TYPE = {
  ...AI_DICT,
  ...APS_DICT,
  ...BPM_DICT,
  ...CRM_DICT,
  ...ERP_DICT,
  ...FMS_DICT,
  ...INFRA_DICT,
  ...IM_DICT,
  ...IOT_DICT,
  ...HRM_DICT,
  ...MDM_DICT,
  ...MES_DICT,
  ...WMS_DICT,
  ...MEMBER_DICT,
  ...MP_DICT,
  ...PAY_DICT,
  ...MALL_DICT,
  ...SYSTEM_DICT,
  ...COMMON_DICT,
} as const;

export { DICT_TYPE };
