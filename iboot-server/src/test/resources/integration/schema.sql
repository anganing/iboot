-- ============================================================================
-- schema.sql —— 跨模块集成测试 H2 建表脚本（本文件由脚本生成，禁止手工编辑）
-- 生成方式：python script/h2-schema-convert.py
-- 来源：开发库 ruoyi-vue-pro-20260817 全量 SHOW CREATE TABLE（155 张业务表）
-- 排除：ACT_/FLW_（Flowable 引擎自建）、QRTZ_（Quartz 走 memory）、demo_（演示表）
-- 转换规则：
--   1. json/text/mediumtext/longtext -> CLOB；mediumblob/longblob/tinyblob/blob -> BLOB
--   2. bit(1) -> BOOLEAN（DEFAULT b'0' -> DEFAULT FALSE）；bit(N>1) -> BINARY(ceil(N/8))
--   3. 删除列级/索引级/表级 COMMENT、列级 CHARACTER SET / COLLATE、ON UPDATE CURRENT_TIMESTAMP
--   4. 删除表级 ENGINE / AUTO_INCREMENT=N / DEFAULT CHARSET / COLLATE / ROW_FORMAT
--   5. 删除 unsigned / zerofill；整型显示宽度归一（int(11) -> int）
--   6. 索引 USING BTREE 移除；反引号移除
--   7. 全部 CREATE TABLE IF NOT EXISTS（幂等）
-- ============================================================================

-- ----------------------------
-- aps_command_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_command_record (
  id bigint NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  command_type varchar(64) NOT NULL,

  source_module varchar(32) NOT NULL,

  source_biz_no varchar(64) NOT NULL,

  source_biz_line_no varchar(64) DEFAULT NULL,

  result_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_command_record_uk_aps_command_idempotency_deleted (idempotency_key,deleted)

);

-- ----------------------------
-- aps_load
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_load (
  id bigint NOT NULL,

  version_id bigint NOT NULL,

  res_id bigint NOT NULL,

  res_code varchar(64) NOT NULL,

  res_name varchar(128) NOT NULL,

  work_date date NOT NULL,

  load_minutes int NOT NULL,

  capacity_minutes int NOT NULL,

  load_rate decimal(10,4) NOT NULL,

  overload_minutes int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_load_uk_aps_load_res_day_deleted (version_id,res_id,work_date,deleted),

  KEY aps_load_idx_aps_load_overload (version_id,overload_minutes)

);

-- ----------------------------
-- aps_projection_inv
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_projection_inv (
  id bigint NOT NULL,

  scenario_id bigint NOT NULL,

  source_balance_id bigint NOT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  loc_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  batch_id bigint NOT NULL,

  lot_no varchar(128) NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  stock_scope varchar(32) NOT NULL,

  quality_status varchar(32) NOT NULL,

  qty_onhand decimal(20,6) NOT NULL,

  qty_reserved decimal(20,6) NOT NULL,

  qty_frozen decimal(20,6) NOT NULL,

  qty_available decimal(20,6) NOT NULL,

  source_update_time datetime NOT NULL,

  snapshot_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_projection_inv_uk_aps_projection_inv_deleted (scenario_id,source_balance_id,deleted),

  KEY aps_projection_inv_idx_aps_projection_inv_available (scenario_id,mat_id,quality_status,project_id),

  KEY aps_projection_inv_idx_aps_projection_inv_batch (scenario_id,batch_id)

);

-- ----------------------------
-- aps_projection_mat
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_projection_mat (
  id bigint NOT NULL,

  scenario_id bigint NOT NULL,

  source_mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  mat_type varchar(32) NOT NULL,

  base_uom_id bigint NOT NULL,

  base_uom_code varchar(64) NOT NULL,

  batch_flag BOOLEAN NOT NULL,

  bom_id bigint NOT NULL DEFAULT '0',

  bom_version varchar(32) DEFAULT NULL,

  route_id bigint NOT NULL DEFAULT '0',

  route_version varchar(32) DEFAULT NULL,

  source_update_time datetime NOT NULL,

  snapshot_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_projection_mat_uk_aps_projection_mat_deleted (scenario_id,source_mat_id,deleted),

  KEY aps_projection_mat_idx_aps_projection_mat_code (scenario_id,mat_code)

);

-- ----------------------------
-- aps_projection_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_projection_order (
  id bigint NOT NULL,

  scenario_id bigint NOT NULL,

  source_type varchar(32) NOT NULL,

  source_id bigint NOT NULL,

  source_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL DEFAULT '0',

  work_order_id bigint NOT NULL DEFAULT '0',

  op_task_id bigint NOT NULL DEFAULT '0',

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  route_op_id bigint NOT NULL DEFAULT '0',

  op_id bigint NOT NULL DEFAULT '0',

  op_code varchar(64) NOT NULL DEFAULT '',

  op_name varchar(128) NOT NULL DEFAULT '',

  seq int NOT NULL DEFAULT '0',

  res_id bigint NOT NULL DEFAULT '0',

  qty decimal(20,6) NOT NULL,

  completed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  due_date datetime NOT NULL,

  priority int NOT NULL DEFAULT '0',

  setup_minutes int NOT NULL DEFAULT '0',

  std_minutes_per_unit decimal(18,6) NOT NULL DEFAULT '0.000000',

  teardown_minutes int NOT NULL DEFAULT '0',

  queue_minutes int NOT NULL DEFAULT '0',

  move_minutes int NOT NULL DEFAULT '0',

  duration_type varchar(32) NOT NULL DEFAULT 'PER_UNIT',

  std_duration int NOT NULL DEFAULT '0',

  man_qty int NOT NULL DEFAULT '1',

  changeover_group varchar(64) NOT NULL DEFAULT '',

  material_shortage_flag BOOLEAN NOT NULL DEFAULT FALSE,

  material_short_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  current_status varchar(32) NOT NULL,

  actual_start_time datetime DEFAULT NULL,

  actual_end_time datetime DEFAULT NULL,

  locked_flag BOOLEAN NOT NULL DEFAULT FALSE,

  source_update_time datetime NOT NULL,

  snapshot_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_projection_order_uk_aps_projection_order_deleted (scenario_id,source_type,source_id,deleted),

  KEY aps_projection_order_idx_aps_projection_order_due (scenario_id,current_status,due_date),

  KEY aps_projection_order_idx_aps_projection_order_project (scenario_id,project_id,project_task_id)

);

-- ----------------------------
-- aps_projection_resource
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_projection_resource (
  id bigint NOT NULL,

  scenario_id bigint NOT NULL,

  source_res_id bigint NOT NULL,

  res_code varchar(64) NOT NULL,

  res_name varchar(128) NOT NULL,

  res_type varchar(32) NOT NULL,

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL,

  wc_id bigint NOT NULL,

  work_date date NOT NULL,

  shift_id bigint NOT NULL DEFAULT '0',

  shift_start_time time DEFAULT NULL,

  shift_end_time time DEFAULT NULL,

  available_minutes int NOT NULL,

  capability_json CLOB DEFAULT NULL,

  source_update_time datetime NOT NULL,

  snapshot_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_projection_resource_uk_aps_projection_res_day_deleted (scenario_id,source_res_id,work_date,shift_id,deleted),

  KEY aps_projection_resource_idx_aps_projection_res_wc (scenario_id,wc_id,work_date)

);

-- ----------------------------
-- aps_scenario
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_scenario (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL DEFAULT '0',

  strategy_id bigint NOT NULL,

  plan_start_time datetime NOT NULL,

  plan_end_time datetime NOT NULL,

  snapshot_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_scenario_uk_aps_scenario_code_deleted (code,deleted),

  KEY aps_scenario_idx_aps_scenario_plant_status (plant_id,status),

  KEY aps_scenario_idx_aps_scenario_plan_window (plan_start_time,plan_end_time)

);

-- ----------------------------
-- aps_shortage
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_shortage (
  id bigint NOT NULL,

  version_id bigint NOT NULL,

  prod_order_id bigint NOT NULL DEFAULT '0',

  op_task_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  req_qty decimal(20,6) NOT NULL,

  avail_qty decimal(20,6) NOT NULL,

  short_qty decimal(20,6) NOT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  need_date datetime NOT NULL,

  expected_supply_date datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY aps_shortage_idx_aps_shortage_version_mat (version_id,mat_id,status),

  KEY aps_shortage_idx_aps_shortage_need_date (version_id,need_date),

  KEY aps_shortage_idx_aps_shortage_project (project_id,project_task_id)

);

-- ----------------------------
-- aps_solver_snapshot
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_solver_snapshot (
  id bigint NOT NULL,

  version_id bigint NOT NULL,

  snapshot_type varchar(32) NOT NULL,

  snapshot_json CLOB NOT NULL,

  checksum varchar(64) NOT NULL,

  snapshot_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_solver_snapshot_uk_aps_solver_snapshot_deleted (version_id,snapshot_type,checksum,deleted),

  KEY aps_solver_snapshot_idx_aps_solver_snapshot_time (version_id,snapshot_time)

);

-- ----------------------------
-- aps_strategy
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_strategy (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  version_no varchar(32) NOT NULL,

  objective varchar(255) NOT NULL,

  config_json CLOB NOT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_strategy_uk_aps_strategy_code_ver_deleted (code,version_no,deleted),

  KEY aps_strategy_idx_aps_strategy_status (status)

);

-- ----------------------------
-- aps_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_task (
  id bigint NOT NULL,

  version_id bigint NOT NULL,

  task_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL,

  prod_order_no varchar(64) NOT NULL,

  work_order_id bigint NOT NULL,

  op_task_id bigint NOT NULL,

  route_op_id bigint NOT NULL,

  op_code varchar(64) NOT NULL,

  op_name varchar(128) NOT NULL,

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  res_id bigint NOT NULL,

  res_code varchar(64) NOT NULL,

  start_time datetime NOT NULL,

  end_time datetime NOT NULL,

  duration_minutes int NOT NULL,

  changeover_minutes int NOT NULL DEFAULT '0',

  sequence_no int NOT NULL,

  locked_flag BOOLEAN NOT NULL DEFAULT FALSE,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_task_uk_aps_task_version_op_deleted (version_id,op_task_id,deleted),

  UNIQUE KEY aps_task_uk_aps_task_no_deleted (task_no,deleted),

  KEY aps_task_idx_aps_task_resource_time (version_id,res_id,start_time),

  KEY aps_task_idx_aps_task_order (version_id,prod_order_id,sequence_no)

);

-- ----------------------------
-- aps_task_dep
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_task_dep (
  id bigint NOT NULL,

  version_id bigint NOT NULL,

  from_task_id bigint NOT NULL,

  to_task_id bigint NOT NULL,

  dep_type varchar(32) NOT NULL,

  lag_minutes int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_task_dep_uk_aps_task_dep_deleted (version_id,from_task_id,to_task_id,dep_type,deleted),

  KEY aps_task_dep_idx_aps_task_dep_to (version_id,to_task_id)

);

-- ----------------------------
-- aps_task_lock
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_task_lock (
  id bigint NOT NULL,

  task_id bigint NOT NULL,

  lock_type varchar(32) NOT NULL,

  reason varchar(500) NOT NULL,

  locked_by bigint DEFAULT NULL,

  locked_time datetime NOT NULL,

  unlock_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY aps_task_lock_idx_aps_task_lock_task (task_id,status),

  KEY aps_task_lock_idx_aps_task_lock_time (locked_time)

);

-- ----------------------------
-- aps_version
-- ----------------------------
CREATE TABLE IF NOT EXISTS aps_version (
  id bigint NOT NULL,

  scenario_id bigint NOT NULL,

  version_no int NOT NULL,

  status varchar(32) NOT NULL,

  score varchar(255) DEFAULT NULL,

  hard_score bigint DEFAULT NULL,

  soft_score bigint DEFAULT NULL,

  solve_start_time datetime DEFAULT NULL,

  solve_end_time datetime DEFAULT NULL,

  published_time datetime DEFAULT NULL,

  published_by bigint DEFAULT NULL,

  solver_message varchar(1000) DEFAULT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY aps_version_uk_aps_version_scenario_no_deleted (scenario_id,version_no,deleted),

  KEY aps_version_idx_aps_version_status (scenario_id,status)

);

-- ----------------------------
-- bpm_category
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_category (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) DEFAULT '',

  code varchar(30) DEFAULT '',

  description varchar(255) NOT NULL DEFAULT '',

  status tinyint DEFAULT NULL,

  sort int DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_form
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_form (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(64) NOT NULL,

  status tinyint NOT NULL,

  conf CLOB NOT NULL,

  fields CLOB NOT NULL,

  remark varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_oa_leave
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_oa_leave (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  type tinyint NOT NULL,

  reason varchar(200) NOT NULL,

  start_time datetime NOT NULL,

  end_time datetime NOT NULL,

  day tinyint NOT NULL,

  status tinyint NOT NULL,

  process_instance_id varchar(64) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_process_definition_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_process_definition_info (
  id bigint NOT NULL AUTO_INCREMENT,

  process_definition_id varchar(64) NOT NULL,

  model_id varchar(64) NOT NULL,

  model_type tinyint NOT NULL DEFAULT '10',

  category varchar(64) NOT NULL,

  icon varchar(512) DEFAULT NULL,

  description varchar(255) DEFAULT NULL,

  form_type tinyint NOT NULL,

  form_id bigint DEFAULT NULL,

  form_conf CLOB,

  form_fields CLOB,

  form_custom_create_path varchar(255) DEFAULT NULL,

  form_custom_view_path varchar(255) DEFAULT NULL,

  simple_model CLOB,

  sort bigint DEFAULT '0',

  visible BOOLEAN NOT NULL DEFAULT TRUE,

  start_user_ids varchar(256) DEFAULT NULL,

  start_dept_ids varchar(256) DEFAULT NULL,

  manager_user_ids varchar(256) DEFAULT NULL,

  allow_cancel_running_process BOOLEAN NOT NULL DEFAULT TRUE,

  allow_withdraw_task BOOLEAN NOT NULL DEFAULT FALSE,

  process_id_rule varchar(255) DEFAULT NULL,

  auto_approval_type tinyint NOT NULL DEFAULT '0',

  title_setting varchar(512) DEFAULT NULL,

  summary_setting varchar(512) DEFAULT NULL,

  process_before_trigger_setting varchar(1024) DEFAULT NULL,

  process_after_trigger_setting varchar(1024) DEFAULT NULL,

  task_before_trigger_setting varchar(1024) DEFAULT NULL,

  task_after_trigger_setting varchar(1024) DEFAULT NULL,

  print_template_setting CLOB DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_process_expression
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_process_expression (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(64) NOT NULL DEFAULT '',

  status tinyint NOT NULL,

  expression varchar(1024) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_process_instance_copy
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_process_instance_copy (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL DEFAULT '0',

  start_user_id bigint NOT NULL DEFAULT '0',

  process_instance_id varchar(64) NOT NULL DEFAULT '',

  process_instance_name varchar(64) NOT NULL DEFAULT '',

  process_definition_id varchar(64) NOT NULL,

  category varchar(64) NOT NULL,

  activity_id varchar(64) NOT NULL DEFAULT '',

  activity_name varchar(64) NOT NULL,

  task_id varchar(64) DEFAULT '',

  reason varchar(256) DEFAULT '',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_process_listener
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_process_listener (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL DEFAULT '',

  type varchar(255) NOT NULL,

  status tinyint NOT NULL,

  event varchar(30) NOT NULL DEFAULT '',

  value_type varchar(64) NOT NULL DEFAULT '',

  value varchar(1024) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- bpm_user_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS bpm_user_group (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL DEFAULT '',

  description varchar(255) NOT NULL DEFAULT '',

  user_ids varchar(1024) DEFAULT NULL,

  status tinyint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_api_access_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_api_access_log (
  id bigint NOT NULL AUTO_INCREMENT,

  trace_id varchar(64) NOT NULL DEFAULT '',

  user_id bigint NOT NULL DEFAULT '0',

  user_type tinyint NOT NULL DEFAULT '0',

  application_name varchar(50) NOT NULL,

  request_method varchar(16) NOT NULL DEFAULT '',

  request_url varchar(255) NOT NULL DEFAULT '',

  request_params CLOB,

  response_body CLOB,

  user_ip varchar(50) NOT NULL,

  user_agent varchar(512) NOT NULL,

  operate_module varchar(50) DEFAULT NULL,

  operate_name varchar(50) DEFAULT NULL,

  operate_type tinyint DEFAULT '0',

  begin_time datetime NOT NULL,

  end_time datetime NOT NULL,

  duration int NOT NULL,

  result_code int NOT NULL DEFAULT '0',

  result_msg varchar(512) DEFAULT '',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY infra_api_access_log_idx_create_time (create_time)

);

-- ----------------------------
-- infra_api_error_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_api_error_log (
  id bigint NOT NULL AUTO_INCREMENT,

  trace_id varchar(64) NOT NULL,

  user_id bigint NOT NULL DEFAULT '0',

  user_type tinyint NOT NULL DEFAULT '0',

  application_name varchar(50) NOT NULL,

  request_method varchar(16) NOT NULL,

  request_url varchar(255) NOT NULL,

  request_params varchar(8000) NOT NULL,

  user_ip varchar(50) NOT NULL,

  user_agent varchar(512) NOT NULL,

  exception_time datetime NOT NULL,

  exception_name varchar(128) NOT NULL DEFAULT '',

  exception_message CLOB NOT NULL,

  exception_root_cause_message CLOB NOT NULL,

  exception_stack_trace CLOB NOT NULL,

  exception_class_name varchar(512) NOT NULL,

  exception_file_name varchar(512) NOT NULL,

  exception_method_name varchar(512) NOT NULL,

  exception_line_number int NOT NULL,

  process_status tinyint NOT NULL,

  process_time datetime DEFAULT NULL,

  process_user_id int DEFAULT '0',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY infra_api_error_log_idx_create_time (create_time)

);

-- ----------------------------
-- infra_code_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_code_rule (
  id bigint NOT NULL AUTO_INCREMENT,

  rule_code varchar(100) NOT NULL,

  name varchar(100) NOT NULL,

  prefix varchar(100) NOT NULL DEFAULT '',

  year_enabled tinyint NOT NULL DEFAULT '0',

  month_enabled tinyint NOT NULL DEFAULT '0',

  day_enabled tinyint NOT NULL DEFAULT '0',

  suffix varchar(100) NOT NULL DEFAULT '',

  serial_length int NOT NULL,

  serial_start bigint NOT NULL,

  serial_step bigint NOT NULL,

  reset_cycle tinyint NOT NULL DEFAULT '0',

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY infra_code_rule_uk_rule_code_deleted (rule_code,deleted)

);

-- ----------------------------
-- infra_code_sequence
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_code_sequence (
  id bigint NOT NULL AUTO_INCREMENT,

  rule_id bigint NOT NULL,

  period_key varchar(8) NOT NULL,

  current_no bigint NOT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY infra_code_sequence_uk_rule_period (rule_id,period_key)

);

-- ----------------------------
-- infra_codegen_column
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_codegen_column (
  id bigint NOT NULL AUTO_INCREMENT,

  table_id bigint NOT NULL,

  column_name varchar(200) NOT NULL,

  data_type varchar(100) NOT NULL,

  column_comment varchar(500) NOT NULL,

  nullable BOOLEAN NOT NULL,

  primary_key BOOLEAN NOT NULL,

  ordinal_position int NOT NULL,

  java_type varchar(32) NOT NULL,

  java_field varchar(64) NOT NULL,

  dict_type varchar(200) DEFAULT '',

  example varchar(64) DEFAULT NULL,

  create_operation BOOLEAN NOT NULL,

  update_operation BOOLEAN NOT NULL,

  list_operation BOOLEAN NOT NULL,

  list_operation_condition varchar(32) NOT NULL DEFAULT '=',

  list_operation_result BOOLEAN NOT NULL,

  html_type varchar(32) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  join_table_name varchar(200) DEFAULT NULL,

  join_display_fields varchar(200) DEFAULT NULL,

  PRIMARY KEY (id),

  KEY infra_codegen_column_idx_table_id (table_id)

);

-- ----------------------------
-- infra_codegen_table
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_codegen_table (
  id bigint NOT NULL AUTO_INCREMENT,

  data_source_config_id bigint NOT NULL,

  scene tinyint NOT NULL DEFAULT '1',

  table_name varchar(200) NOT NULL DEFAULT '',

  table_comment varchar(500) NOT NULL DEFAULT '',

  remark varchar(500) DEFAULT NULL,

  module_name varchar(30) NOT NULL,

  business_name varchar(30) NOT NULL,

  class_name varchar(100) NOT NULL DEFAULT '',

  class_comment varchar(50) NOT NULL,

  author varchar(50) NOT NULL,

  template_type tinyint NOT NULL DEFAULT '1',

  front_type tinyint NOT NULL,

  parent_menu_id bigint DEFAULT NULL,

  master_table_id bigint DEFAULT NULL,

  sub_join_column_id bigint DEFAULT NULL,

  sub_join_many BOOLEAN DEFAULT NULL,

  tree_parent_column_id bigint DEFAULT NULL,

  tree_name_column_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_config (
  id bigint NOT NULL AUTO_INCREMENT,

  category varchar(50) NOT NULL,

  type tinyint NOT NULL,

  name varchar(100) NOT NULL DEFAULT '',

  config_key varchar(100) NOT NULL DEFAULT '',

  value varchar(500) NOT NULL DEFAULT '',

  visible BOOLEAN NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY infra_config_idx_config_key (config_key)

);

-- ----------------------------
-- infra_data_source_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_data_source_config (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(100) NOT NULL DEFAULT '',

  url varchar(1024) NOT NULL,

  username varchar(255) NOT NULL,

  password varchar(255) NOT NULL DEFAULT '',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_file
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_file (
  id bigint NOT NULL AUTO_INCREMENT,

  config_id bigint DEFAULT NULL,

  name varchar(256) DEFAULT NULL,

  path varchar(512) NOT NULL,

  url varchar(1024) NOT NULL,

  type varchar(128) DEFAULT NULL,

  size int NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_file_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_file_config (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(63) NOT NULL,

  storage tinyint NOT NULL,

  remark varchar(255) DEFAULT NULL,

  master BOOLEAN NOT NULL,

  config varchar(4096) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_file_content
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_file_content (
  id bigint NOT NULL AUTO_INCREMENT,

  config_id bigint NOT NULL,

  path varchar(512) NOT NULL,

  content BLOB NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY infra_file_content_idx_config_id_path (config_id,path)

);

-- ----------------------------
-- infra_job
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_job (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(32) NOT NULL,

  status tinyint NOT NULL,

  handler_name varchar(64) NOT NULL,

  handler_param varchar(255) DEFAULT NULL,

  cron_expression varchar(32) NOT NULL,

  retry_count int NOT NULL DEFAULT '0',

  retry_interval int NOT NULL DEFAULT '0',

  monitor_timeout int NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- infra_job_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS infra_job_log (
  id bigint NOT NULL AUTO_INCREMENT,

  job_id bigint NOT NULL,

  handler_name varchar(64) NOT NULL,

  handler_param varchar(255) DEFAULT NULL,

  execute_index tinyint NOT NULL DEFAULT '1',

  begin_time datetime NOT NULL,

  end_time datetime DEFAULT NULL,

  duration int DEFAULT NULL,

  status tinyint NOT NULL,

  result varchar(4000) DEFAULT '',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY infra_job_log_idx_job_id (job_id),

  KEY infra_job_log_idx_create_time (create_time)

);

-- ----------------------------
-- mdm_bom
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_bom (
  id bigint NOT NULL,

  mat_id bigint NOT NULL,

  version varchar(32) NOT NULL,

  status tinyint NOT NULL DEFAULT '1',

  effective_date date NOT NULL,

  expiry_date date DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_bom_uk_mdm_bom_mat_version_deleted (mat_id,version,deleted)

);

-- ----------------------------
-- mdm_bom_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_bom_line (
  id bigint NOT NULL,

  bom_id bigint NOT NULL,

  line_no int NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  qty decimal(18,6) NOT NULL,

  scrap_rate decimal(8,6) NOT NULL DEFAULT '0.000000',

  issue_method varchar(32) NOT NULL DEFAULT 'PICK',

  route_op_seq int DEFAULT NULL,

  position_no varchar(64) DEFAULT NULL,

  effective_date date DEFAULT NULL,

  expiry_date date DEFAULT NULL,

  uom_id bigint NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_bom_line_uk_mdm_bom_line_bom_line_deleted (bom_id,line_no,deleted)

);

-- ----------------------------
-- mdm_calendar
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_calendar (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_calendar_uk_mdm_calendar_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_calendar_exception
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_calendar_exception (
  id bigint NOT NULL,

  calendar_id bigint NOT NULL,

  exception_date date NOT NULL,

  exception_type varchar(32) NOT NULL,

  shift_id bigint DEFAULT NULL,

  available_minutes int DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_calendar_exception_uk_mdm_calendar_exc_cal_date_shift_deleted (calendar_id,exception_date,shift_id,deleted),

  KEY mdm_calendar_exception_idx_mdm_calendar_exception_date (exception_date)

);

-- ----------------------------
-- mdm_calendar_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_calendar_rule (
  id bigint NOT NULL,

  calendar_id bigint NOT NULL,

  day_of_week tinyint NOT NULL,

  shift_id bigint NOT NULL,

  available_minutes int NOT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_calendar_rule_uk_mdm_calendar_rule_cal_day_shift_deleted (calendar_id,day_of_week,shift_id,deleted),

  KEY mdm_calendar_rule_idx_mdm_calendar_rule_shift_id (shift_id)

);

-- ----------------------------
-- mdm_customer
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_customer (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_customer_uk_mdm_customer_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_defect_code
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_defect_code (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  level varchar(32) NOT NULL,

  parent_id bigint NOT NULL DEFAULT '0',

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_defect_code_uk_mdm_defect_code_code_deleted (code,deleted),

  KEY mdm_defect_code_idx_mdm_defect_code_parent_id (parent_id)

);

-- ----------------------------
-- mdm_defect_code_mat_cat
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_defect_code_mat_cat (
  id bigint NOT NULL AUTO_INCREMENT,

  defect_code_id bigint NOT NULL,

  mat_cat_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_defect_code_mat_cat_uk_mdm_defect_code_mat_cat (defect_code_id,mat_cat_id,deleted)

);

-- ----------------------------
-- mdm_defect_code_op
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_defect_code_op (
  id bigint NOT NULL AUTO_INCREMENT,

  defect_code_id bigint NOT NULL,

  op_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_defect_code_op_uk_mdm_defect_code_op (defect_code_id,op_id,deleted)

);

-- ----------------------------
-- mdm_inv_org
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_inv_org (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_inv_org_uk_mdm_inv_org_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_mat
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_mat (
  id bigint NOT NULL,

  cat_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  spec varchar(255) DEFAULT NULL,

  drawing_no varchar(128) DEFAULT NULL,

  mat_type varchar(32) NOT NULL,

  base_uom_id bigint NOT NULL,

  batch_flag BOOLEAN NOT NULL DEFAULT FALSE,

  sn_flag BOOLEAN NOT NULL DEFAULT FALSE,

  net_weight decimal(20,6) DEFAULT NULL,

  gross_weight decimal(20,6) DEFAULT NULL,

  weight_uom_id bigint DEFAULT NULL,

  length decimal(20,6) DEFAULT NULL,

  width decimal(20,6) DEFAULT NULL,

  height decimal(20,6) DEFAULT NULL,

  volume decimal(20,6) DEFAULT NULL,

  plan_strategy varchar(32) DEFAULT NULL,

  lead_time_days int DEFAULT NULL,

  safety_stock decimal(20,6) DEFAULT NULL,

  min_order_qty decimal(20,6) DEFAULT NULL,

  order_multiple decimal(20,6) DEFAULT NULL,

  default_wh_id bigint DEFAULT NULL,

  default_loc_id bigint DEFAULT NULL,

  pack_qty decimal(20,6) DEFAULT NULL,

  abc_class varchar(8) DEFAULT NULL,

  changeover_group varchar(64) DEFAULT NULL,

  shelf_life_days int DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_mat_uk_mdm_mat_code_deleted (code,deleted),

  KEY mdm_mat_idx_mdm_mat_cat_id (cat_id),

  KEY mdm_mat_idx_mdm_mat_base_uom_id (base_uom_id)

);

-- ----------------------------
-- mdm_mat_alt
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_mat_alt (
  id bigint NOT NULL,

  mat_id bigint NOT NULL,

  alt_mat_id bigint NOT NULL,

  priority int NOT NULL DEFAULT '1',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_mat_alt_uk_mdm_mat_alt_mat_alt_deleted (mat_id,alt_mat_id,deleted)

);

-- ----------------------------
-- mdm_mat_cat
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_mat_cat (
  id bigint NOT NULL,

  parent_id bigint NOT NULL DEFAULT '0',

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_mat_cat_uk_mdm_mat_cat_code_deleted (code,deleted),

  KEY mdm_mat_cat_idx_mdm_mat_cat_parent_id (parent_id)

);

-- ----------------------------
-- mdm_member_skill
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_member_skill (
  id bigint NOT NULL,

  user_id bigint NOT NULL,

  skill_id bigint NOT NULL,

  level int NOT NULL DEFAULT '1',

  effective_date date DEFAULT NULL,

  expiry_date date DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_member_skill_uk_mdm_member_skill_user_skill_deleted (user_id,skill_id,deleted),

  KEY mdm_member_skill_idx_mdm_member_skill_skill_id (skill_id)

);

-- ----------------------------
-- mdm_op
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_op (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  std_duration int NOT NULL DEFAULT '0',

  setup_minutes int DEFAULT NULL,

  std_minutes_per_unit decimal(18,6) DEFAULT NULL,

  teardown_minutes int DEFAULT NULL,

  queue_minutes int DEFAULT NULL,

  move_minutes int DEFAULT NULL,

  duration_type varchar(32) NOT NULL DEFAULT 'FIXED',

  man_qty int DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_op_uk_mdm_op_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_op_skill_req
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_op_skill_req (
  id bigint NOT NULL,

  op_id bigint NOT NULL,

  skill_id bigint NOT NULL,

  min_level int NOT NULL DEFAULT '1',

  status tinyint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_op_skill_req_uk_mdm_op_skill_req_op_skill_deleted (op_id,skill_id,deleted),

  KEY mdm_op_skill_req_idx_mdm_op_skill_req_skill_id (skill_id)

);

-- ----------------------------
-- mdm_plant
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_plant (
  id bigint NOT NULL,

  inv_org_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  address varchar(255) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_plant_uk_mdm_plant_code_deleted (code,deleted),

  KEY mdm_plant_idx_mdm_plant_inv_org_id (inv_org_id)

);

-- ----------------------------
-- mdm_project
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_project (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_project_uk_mdm_project_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_project_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_project_task (
  id bigint NOT NULL,

  project_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_project_task_uk_mdm_project_task_project_code_deleted (project_id,code,deleted),

  KEY mdm_project_task_idx_mdm_project_task_project_id (project_id)

);

-- ----------------------------
-- mdm_res
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_res (
  id bigint NOT NULL,

  wc_id bigint NOT NULL,

  res_type varchar(32) NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  efficiency decimal(8,4) NOT NULL DEFAULT '1.0000',

  parallel_capacity int NOT NULL DEFAULT '1',

  res_group varchar(64) DEFAULT NULL,

  calendar_id bigint DEFAULT NULL,

  plant_id bigint DEFAULT NULL,

  workshop_id bigint DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_res_uk_mdm_res_code_deleted (code,deleted),

  KEY mdm_res_idx_mdm_res_wc_id (wc_id)

);

-- ----------------------------
-- mdm_res_calendar
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_res_calendar (
  id bigint NOT NULL,

  res_id bigint NOT NULL,

  work_date date NOT NULL,

  shift_id bigint NOT NULL,

  source_calendar_id bigint NOT NULL DEFAULT '0',

  available_minutes int NOT NULL,

  generated_flag BOOLEAN NOT NULL DEFAULT FALSE,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_res_calendar_uk_mdm_res_calendar_res_date_shift_deleted (res_id,work_date,shift_id,deleted)

);

-- ----------------------------
-- mdm_res_capability
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_res_capability (
  id bigint NOT NULL,

  res_id bigint NOT NULL,

  op_id bigint NOT NULL,

  preferred_flag BOOLEAN NOT NULL DEFAULT FALSE,

  efficiency decimal(8,4) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_res_capability_uk_mdm_res_capability_res_op_deleted (res_id,op_id,deleted),

  KEY mdm_res_capability_idx_mdm_res_capability_op_id (op_id)

);

-- ----------------------------
-- mdm_res_changeover
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_res_changeover (
  id bigint NOT NULL,

  res_id bigint NOT NULL DEFAULT '0',

  res_group varchar(64) DEFAULT NULL,

  from_group varchar(64) NOT NULL,

  to_group varchar(64) NOT NULL,

  changeover_minutes int NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_res_changeover_uk_mdm_res_changeover_res_from_to_deleted (res_id,from_group,to_group,deleted),

  KEY mdm_res_changeover_idx_mdm_res_changeover_res_group (res_group)

);

-- ----------------------------
-- mdm_route
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route (
  id bigint NOT NULL,

  mat_id bigint NOT NULL,

  version varchar(32) NOT NULL,

  status tinyint NOT NULL DEFAULT '1',

  effective_date date NOT NULL,

  expiry_date date DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_route_uk_mdm_route_mat_version_deleted (mat_id,version,deleted)

);

-- ----------------------------
-- mdm_route_op
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route_op (
  id bigint NOT NULL,

  route_id bigint NOT NULL,

  seq int NOT NULL,

  op_id bigint NOT NULL,

  wc_id bigint NOT NULL,

  parallel_group varchar(64) DEFAULT NULL,

  key_flag BOOLEAN NOT NULL DEFAULT FALSE,

  fai_flag BOOLEAN NOT NULL DEFAULT FALSE,

  skip_policy varchar(32) NOT NULL DEFAULT 'NOT_ALLOWED',

  override_setup_minutes int DEFAULT NULL,

  override_std_minutes_per_unit decimal(18,6) DEFAULT NULL,

  override_queue_minutes int DEFAULT NULL,

  override_move_minutes int DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_route_op_uk_mdm_route_op_route_seq_deleted (route_id,seq,deleted)

);

-- ----------------------------
-- mdm_route_op_esop
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route_op_esop (
  id bigint NOT NULL,

  route_op_id bigint NOT NULL,

  seq int NOT NULL,

  title varchar(128) NOT NULL,

  content CLOB,

  param_json CLOB,

  required_confirm_flag BOOLEAN NOT NULL DEFAULT FALSE,

  status tinyint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_route_op_esop_uk_mdm_route_op_esop_route_op_seq_deleted (route_op_id,seq,deleted),

  KEY mdm_route_op_esop_idx_mdm_route_op_esop_route_op_id (route_op_id)

);

-- ----------------------------
-- mdm_route_op_esop_file
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route_op_esop_file (
  id bigint NOT NULL AUTO_INCREMENT,

  esop_id bigint NOT NULL,

  seq int NOT NULL DEFAULT '1',

  file_type varchar(32) NOT NULL,

  file_name varchar(255) NOT NULL,

  file_url varchar(512) NOT NULL,

  file_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY mdm_route_op_esop_file_idx_mdm_route_op_esop_file_esop_id (esop_id)

);

-- ----------------------------
-- mdm_route_op_mat
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route_op_mat (
  id bigint NOT NULL,

  route_op_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  qty decimal(18,6) NOT NULL,

  scrap_rate decimal(8,6) NOT NULL DEFAULT '0.000000',

  issue_method varchar(32) NOT NULL DEFAULT 'PICK',

  must_issue_before_start BOOLEAN NOT NULL DEFAULT FALSE,

  uom_id bigint NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_route_op_mat_uk_mdm_route_op_mat_op_mat_deleted (route_op_id,mat_id,deleted)

);

-- ----------------------------
-- mdm_route_op_tool
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_route_op_tool (
  id bigint NOT NULL,

  route_op_id bigint NOT NULL,

  tool_id bigint NOT NULL,

  qty int NOT NULL DEFAULT '1',

  required_flag BOOLEAN NOT NULL DEFAULT TRUE,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_route_op_tool_uk_mdm_route_op_tool_op_tool_deleted (route_op_id,tool_id,deleted),

  KEY mdm_route_op_tool_idx_mdm_route_op_tool_tool_id (tool_id)

);

-- ----------------------------
-- mdm_shift
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_shift (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  start_time time NOT NULL,

  end_time time NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_shift_uk_mdm_shift_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_skill
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_skill (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  level_desc varchar(500) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_skill_uk_mdm_skill_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_supplier
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_supplier (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_supplier_uk_mdm_supplier_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_team
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_team (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  workshop_id bigint DEFAULT NULL,

  shift_id bigint DEFAULT NULL,

  leader_user_id bigint DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_team_uk_mdm_team_code_deleted (code,deleted),

  KEY mdm_team_idx_mdm_team_workshop_id (workshop_id)

);

-- ----------------------------
-- mdm_team_member
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_team_member (
  id bigint NOT NULL,

  team_id bigint NOT NULL,

  user_id bigint NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_team_member_uk_mdm_team_member_team_user_deleted (team_id,user_id,deleted),

  KEY mdm_team_member_idx_mdm_team_member_user_id (user_id)

);

-- ----------------------------
-- mdm_tool
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_tool (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  tool_type varchar(32) NOT NULL,

  wc_id bigint DEFAULT NULL,

  qty_total int NOT NULL DEFAULT '1',

  life_type varchar(32) DEFAULT NULL,

  life_limit int DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_tool_uk_mdm_tool_code_deleted (code,deleted),

  KEY mdm_tool_idx_mdm_tool_wc_id (wc_id)

);

-- ----------------------------
-- mdm_uom
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_uom (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_uom_uk_mdm_uom_code_deleted (code,deleted)

);

-- ----------------------------
-- mdm_uom_conv
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_uom_conv (
  id bigint NOT NULL,

  mat_id bigint NOT NULL,

  from_uom_id bigint NOT NULL,

  to_uom_id bigint NOT NULL,

  ratio decimal(18,6) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_uom_conv_uk_mdm_uom_conv_mat_from_to_deleted (mat_id,from_uom_id,to_uom_id,deleted)

);

-- ----------------------------
-- mdm_wc
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_wc (
  id bigint NOT NULL,

  workshop_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_wc_uk_mdm_wc_code_deleted (code,deleted),

  KEY mdm_wc_idx_mdm_wc_workshop_id (workshop_id)

);

-- ----------------------------
-- mdm_workshop
-- ----------------------------
CREATE TABLE IF NOT EXISTS mdm_workshop (
  id bigint NOT NULL,

  plant_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mdm_workshop_uk_mdm_workshop_code_deleted (code,deleted),

  KEY mdm_workshop_idx_mdm_workshop_plant_id (plant_id)

);

-- ----------------------------
-- mes_command_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_command_record (
  id bigint NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  command_type varchar(64) NOT NULL,

  source_module varchar(32) NOT NULL,

  source_biz_no varchar(64) NOT NULL,

  source_biz_line_no varchar(64) DEFAULT NULL,

  result_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_command_record_uk_mes_command_idempotency_deleted (idempotency_key,deleted)

);

-- ----------------------------
-- mes_defect
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_defect (
  id bigint NOT NULL,

  task_id bigint NOT NULL,

  result_id bigint DEFAULT NULL,

  defect_code_id bigint NOT NULL DEFAULT '0',

  defect_code varchar(64) NOT NULL,

  defect_name varchar(128) NOT NULL,

  defect_level varchar(32) DEFAULT NULL,

  qty decimal(20,6) NOT NULL,

  description varchar(500) DEFAULT NULL,

  location varchar(255) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY mes_defect_idx_mes_defect_task (task_id,defect_code),

  KEY mes_defect_idx_mes_defect_result (result_id)

);

-- ----------------------------
-- mes_defect_file
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_defect_file (
  id bigint NOT NULL AUTO_INCREMENT,

  defect_id bigint NOT NULL,

  seq int NOT NULL,

  file_type varchar(32) NOT NULL,

  file_name varchar(255) NOT NULL,

  file_url varchar(512) NOT NULL,

  file_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY mes_defect_file_idx_mes_defect_file_defect_id (defect_id)

);

-- ----------------------------
-- mes_dispatch_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_dispatch_task (
  id bigint NOT NULL,

  dispatch_no varchar(64) NOT NULL,

  op_task_id bigint NOT NULL,

  operator_id bigint DEFAULT NULL,

  team_id bigint DEFAULT NULL,

  res_id bigint NOT NULL,

  shift_id bigint DEFAULT NULL,

  planned_start_time datetime DEFAULT NULL,

  planned_end_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_dispatch_task_uk_mes_dispatch_no_deleted (dispatch_no,deleted),

  KEY mes_dispatch_task_idx_mes_dispatch_task (op_task_id,status),

  KEY mes_dispatch_task_idx_mes_dispatch_operator (operator_id,status)

);

-- ----------------------------
-- mes_disposition
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_disposition (
  id bigint NOT NULL,

  disposition_no varchar(64) NOT NULL,

  task_id bigint NOT NULL,

  disposition_type varchar(32) NOT NULL,

  qty decimal(20,6) NOT NULL,

  target_biz_type varchar(32) DEFAULT NULL,

  target_biz_no varchar(64) DEFAULT NULL,

  approved_by bigint DEFAULT NULL,

  approve_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  approval_status int NOT NULL DEFAULT '-1',

  process_instance_id varchar(64) DEFAULT NULL,

  approval_time datetime DEFAULT NULL,

  approval_reason varchar(500) DEFAULT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY mes_disposition_uk_mes_disposition_no_deleted (disposition_no,deleted),

  UNIQUE KEY mes_disposition_uk_mes_disposition_idem_deleted (idempotency_key,deleted),

  UNIQUE KEY mes_disposition_uk_process_instance_id (process_instance_id),

  KEY mes_disposition_idx_mes_disposition_task (task_id,status),

  KEY mes_disposition_idx_mes_disposition_target (target_biz_type,target_biz_no)

);

-- ----------------------------
-- mes_kit_snapshot
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_kit_snapshot (
  id bigint NOT NULL,

  check_no varchar(64) NOT NULL,

  check_scope varchar(32) NOT NULL,

  prod_order_id bigint NOT NULL,

  work_order_id bigint NOT NULL DEFAULT '0',

  op_task_id bigint NOT NULL DEFAULT '0',

  line_no int NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  req_qty decimal(20,6) NOT NULL,

  avail_qty decimal(20,6) NOT NULL,

  reserved_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  issued_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  short_qty decimal(20,6) NOT NULL,

  alt_mat_id bigint NOT NULL DEFAULT '0',

  result varchar(32) NOT NULL,

  check_time datetime NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_kit_snapshot_uk_mes_kit_snapshot_line_deleted (check_no,line_no,deleted),

  KEY mes_kit_snapshot_idx_mes_kit_snapshot_prod (prod_order_id,check_time),

  KEY mes_kit_snapshot_idx_mes_kit_snapshot_op (op_task_id,check_time),

  KEY mes_kit_snapshot_idx_mes_kit_snapshot_mat (mat_id,result)

);

-- ----------------------------
-- mes_material_req
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_material_req (
  id bigint NOT NULL,

  req_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL,

  prod_order_no varchar(64) NOT NULL,

  work_order_id bigint NOT NULL DEFAULT '0',

  op_task_id bigint NOT NULL DEFAULT '0',

  req_type varchar(32) NOT NULL,

  inv_org_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  wms_biz_no varchar(64) DEFAULT NULL,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  approval_status int NOT NULL DEFAULT '-1',

  process_instance_id varchar(64) DEFAULT NULL,

  approval_time datetime DEFAULT NULL,

  approval_reason varchar(500) DEFAULT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY mes_material_req_uk_mes_material_req_no_deleted (req_no,deleted),

  UNIQUE KEY mes_material_req_uk_mes_material_req_idem_deleted (idempotency_key,deleted),

  UNIQUE KEY mes_material_req_uk_process_instance_id (process_instance_id),

  KEY mes_material_req_idx_mes_material_req_prod (prod_order_id,status),

  KEY mes_material_req_idx_mes_material_req_op (op_task_id,status)

);

-- ----------------------------
-- mes_material_req_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_material_req_line (
  id bigint NOT NULL,

  req_id bigint NOT NULL,

  line_no int NOT NULL,

  route_op_mat_id bigint DEFAULT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  qty decimal(20,6) NOT NULL,

  processed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  batch_id bigint DEFAULT NULL,

  lot_no varchar(128) DEFAULT NULL,

  reservation_id bigint NOT NULL DEFAULT '0',

  must_issue_before_start BOOLEAN NOT NULL DEFAULT TRUE,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_material_req_line_uk_mes_material_req_line_deleted (req_id,line_no,deleted),

  KEY mes_material_req_line_idx_mes_material_req_line_mat (mat_id,status)

);

-- ----------------------------
-- mes_op_skip_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_op_skip_record (
  id bigint NOT NULL,

  op_task_id bigint NOT NULL,

  skip_reason_type varchar(32) NOT NULL,

  skip_reason varchar(500) NOT NULL,

  operator_id bigint NOT NULL,

  approved_by bigint DEFAULT NULL,

  approve_status varchar(32) NOT NULL,

  skip_time datetime DEFAULT NULL,

  effect_remark varchar(500) DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  approval_status int NOT NULL DEFAULT '-1',

  process_instance_id varchar(64) DEFAULT NULL,

  approval_time datetime DEFAULT NULL,

  approval_reason varchar(500) DEFAULT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY mes_op_skip_record_uk_process_instance_id (process_instance_id),

  KEY mes_op_skip_record_idx_mes_op_skip_task (op_task_id,status),

  KEY mes_op_skip_record_idx_mes_op_skip_approve (approve_status,approved_by)

);

-- ----------------------------
-- mes_op_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_op_task (
  id bigint NOT NULL,

  task_no varchar(64) NOT NULL,

  work_order_id bigint NOT NULL,

  work_order_no varchar(64) NOT NULL,

  route_op_id bigint NOT NULL,

  op_id bigint NOT NULL,

  op_code varchar(64) NOT NULL,

  op_name varchar(128) NOT NULL,

  seq int NOT NULL,

  parallel_group varchar(64) DEFAULT NULL,

  prev_task_id bigint DEFAULT NULL,

  next_task_id bigint DEFAULT NULL,

  exec_mode varchar(32) NOT NULL DEFAULT 'SEQUENTIAL',

  qc_mode varchar(32) NOT NULL DEFAULT 'NONE',

  skip_policy varchar(32) NOT NULL DEFAULT 'NOT_ALLOWED',

  key_flag BOOLEAN NOT NULL DEFAULT FALSE,

  planned_qty decimal(20,6) NOT NULL,

  completed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  scrap_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  res_id bigint DEFAULT NULL,

  planned_start_time datetime DEFAULT NULL,

  planned_end_time datetime DEFAULT NULL,

  actual_start_time datetime DEFAULT NULL,

  actual_end_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_op_task_uk_mes_op_task_no_deleted (task_no,deleted),

  UNIQUE KEY mes_op_task_uk_mes_op_task_route_deleted (work_order_id,route_op_id,deleted),

  KEY mes_op_task_idx_mes_op_task_status_plan (status,planned_start_time),

  KEY mes_op_task_idx_mes_op_task_res (res_id,status)

);

-- ----------------------------
-- mes_prod_demand
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_prod_demand (
  id bigint NOT NULL,

  demand_no varchar(64) NOT NULL,

  source_type varchar(32) NOT NULL,

  source_no varchar(64) DEFAULT NULL,

  plant_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  qty decimal(20,6) NOT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  due_date datetime NOT NULL,

  priority int NOT NULL DEFAULT '0',

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_prod_demand_uk_mes_prod_demand_no_deleted (demand_no,deleted),

  UNIQUE KEY mes_prod_demand_uk_mes_prod_demand_idem_deleted (idempotency_key,deleted),

  KEY mes_prod_demand_idx_mes_prod_demand_due (plant_id,status,due_date),

  KEY mes_prod_demand_idx_mes_prod_demand_project (project_id,project_task_id)

);

-- ----------------------------
-- mes_prod_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_prod_order (
  id bigint NOT NULL,

  order_no varchar(64) NOT NULL,

  demand_id bigint DEFAULT NULL,

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  qty decimal(20,6) NOT NULL,

  completed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  scrap_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  bom_id bigint NOT NULL,

  bom_version varchar(32) NOT NULL,

  route_id bigint NOT NULL,

  route_version varchar(32) NOT NULL,

  due_date datetime NOT NULL,

  priority int NOT NULL DEFAULT '0',

  kit_status varchar(32) NOT NULL DEFAULT 'UNCHECKED',

  status varchar(32) NOT NULL,

  release_time datetime DEFAULT NULL,

  actual_start_time datetime DEFAULT NULL,

  actual_end_time datetime DEFAULT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_prod_order_uk_mes_prod_order_no_deleted (order_no,deleted),

  KEY mes_prod_order_idx_mes_prod_order_demand (demand_id),

  KEY mes_prod_order_idx_mes_prod_order_status_due (plant_id,status,due_date),

  KEY mes_prod_order_idx_mes_prod_order_project (project_id,project_task_id)

);

-- ----------------------------
-- mes_prod_report
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_prod_report (
  id bigint NOT NULL,

  report_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL,

  work_order_id bigint NOT NULL,

  op_task_id bigint NOT NULL,

  worker_id bigint DEFAULT NULL,

  res_id bigint NOT NULL,

  shift_id bigint DEFAULT NULL,

  qty_good decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_scrap decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_rework decimal(20,6) NOT NULL DEFAULT '0.000000',

  start_time datetime NOT NULL,

  end_time datetime NOT NULL,

  report_time datetime NOT NULL,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_prod_report_uk_mes_prod_report_no_deleted (report_no,deleted),

  UNIQUE KEY mes_prod_report_uk_mes_prod_report_idem_deleted (idempotency_key,deleted),

  KEY mes_prod_report_idx_mes_prod_report_op_time (op_task_id,report_time),

  KEY mes_prod_report_idx_mes_prod_report_order (prod_order_id,status)

);

-- ----------------------------
-- mes_qc_item
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_qc_item (
  id bigint NOT NULL,

  plan_id bigint NOT NULL,

  item_code varchar(64) NOT NULL,

  item_name varchar(128) NOT NULL,

  item_type varchar(32) NOT NULL,

  unit varchar(32) DEFAULT NULL,

  std_value varchar(255) DEFAULT NULL,

  upper_limit decimal(20,6) DEFAULT NULL,

  lower_limit decimal(20,6) DEFAULT NULL,

  required_flag BOOLEAN NOT NULL DEFAULT TRUE,

  sort_no int NOT NULL DEFAULT '0',

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_qc_item_uk_mes_qc_item_code_deleted (plan_id,item_code,deleted),

  KEY mes_qc_item_idx_mes_qc_item_sort (plan_id,sort_no)

);

-- ----------------------------
-- mes_qc_plan
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_qc_plan (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  version_no varchar(32) NOT NULL,

  sample_type varchar(32) NOT NULL,

  sample_value decimal(20,6) DEFAULT NULL,

  aql varchar(32) DEFAULT NULL,

  acceptance_rule varchar(500) DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_qc_plan_uk_mes_qc_plan_code_ver_deleted (code,version_no,deleted),

  KEY mes_qc_plan_idx_mes_qc_plan_status (status)

);

-- ----------------------------
-- mes_qc_result
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_qc_result (
  id bigint NOT NULL,

  task_id bigint NOT NULL,

  item_id bigint NOT NULL,

  sample_no int NOT NULL DEFAULT '1',

  item_code varchar(64) NOT NULL,

  item_name varchar(128) NOT NULL,

  actual_value varchar(255) DEFAULT NULL,

  pass_flag BOOLEAN NOT NULL,

  inspector_id bigint NOT NULL,

  inspect_time datetime NOT NULL,

  qty_qualified decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_rejected decimal(20,6) NOT NULL DEFAULT '0.000000',

  conclusion varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_qc_result_uk_mes_qc_result_sample_deleted (task_id,item_id,sample_no,deleted),

  KEY mes_qc_result_idx_mes_qc_result_task_pass (task_id,pass_flag),

  KEY mes_qc_result_idx_mes_qc_result_inspector (inspector_id,inspect_time)

);

-- ----------------------------
-- mes_qc_rule
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_qc_rule (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  trigger_point varchar(32) NOT NULL,

  object_type varchar(32) NOT NULL,

  mat_id bigint NOT NULL DEFAULT '0',

  mat_cat_id bigint NOT NULL DEFAULT '0',

  op_id bigint NOT NULL DEFAULT '0',

  wc_id bigint NOT NULL DEFAULT '0',

  supplier_id bigint NOT NULL DEFAULT '0',

  qc_plan_id bigint NOT NULL,

  qc_mode varchar(32) NOT NULL,

  priority int NOT NULL DEFAULT '0',

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_qc_rule_uk_mes_qc_rule_code_deleted (code,deleted),

  KEY mes_qc_rule_idx_mes_qc_rule_match (trigger_point,object_type,status,priority),

  KEY mes_qc_rule_idx_mes_qc_rule_mat (mat_id,mat_cat_id)

);

-- ----------------------------
-- mes_qc_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_qc_task (
  id bigint NOT NULL,

  task_no varchar(64) NOT NULL,

  rule_id bigint DEFAULT NULL,

  qc_plan_id bigint NOT NULL,

  qc_plan_version varchar(32) NOT NULL,

  trigger_point varchar(32) NOT NULL,

  biz_type varchar(32) NOT NULL,

  biz_no varchar(64) NOT NULL,

  biz_line_id bigint DEFAULT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  batch_id bigint DEFAULT NULL,

  lot_no varchar(128) DEFAULT NULL,

  qty decimal(20,6) NOT NULL,

  sample_qty decimal(20,6) NOT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  assigned_inspector_id bigint DEFAULT NULL,

  inspect_start_time datetime DEFAULT NULL,

  inspect_end_time datetime DEFAULT NULL,

  conclusion varchar(32) DEFAULT NULL,

  qty_qualified decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_rejected decimal(20,6) NOT NULL DEFAULT '0.000000',

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_qc_task_uk_mes_qc_task_no_deleted (task_no,deleted),

  UNIQUE KEY mes_qc_task_uk_mes_qc_task_idem_deleted (idempotency_key,deleted),

  KEY mes_qc_task_idx_mes_qc_task_biz (biz_type,biz_no),

  KEY mes_qc_task_idx_mes_qc_task_mat_batch (mat_id,batch_id),

  KEY mes_qc_task_idx_mes_qc_task_status (trigger_point,status)

);

-- ----------------------------
-- mes_rework
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_rework (
  id bigint NOT NULL,

  rework_no varchar(64) NOT NULL,

  source_qc_task_id bigint NOT NULL,

  prod_order_id bigint NOT NULL,

  work_order_id bigint DEFAULT NULL,

  op_task_id bigint NOT NULL,

  qty decimal(20,6) NOT NULL,

  route_id bigint DEFAULT NULL,

  route_version varchar(32) DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_rework_uk_mes_rework_no_deleted (rework_no,deleted),

  KEY mes_rework_idx_mes_rework_qc_task (source_qc_task_id),

  KEY mes_rework_idx_mes_rework_prod (prod_order_id,status)

);

-- ----------------------------
-- mes_rgv_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_rgv_task (
  id bigint NOT NULL,

  task_no varchar(64) NOT NULL,

  external_task_no varchar(128) DEFAULT NULL,

  task_type varchar(32) NOT NULL,

  work_order_id bigint NOT NULL,

  op_task_id bigint NOT NULL,

  target_op_task_id bigint DEFAULT NULL,

  from_station_id bigint NOT NULL,

  to_station_id bigint NOT NULL,

  carrier_no varchar(128) DEFAULT NULL,

  external_system_code varchar(64) NOT NULL,

  dispatch_status varchar(32) NOT NULL,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  completed_time datetime DEFAULT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_rgv_task_uk_mes_rgv_task_no_deleted (task_no,deleted),

  UNIQUE KEY mes_rgv_task_uk_mes_rgv_task_idem_deleted (idempotency_key,deleted),

  UNIQUE KEY mes_rgv_task_uk_mes_rgv_external_deleted (external_system_code,external_task_no,deleted),

  KEY mes_rgv_task_idx_mes_rgv_task_op (op_task_id,status)

);

-- ----------------------------
-- mes_wip_txn
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_wip_txn (
  id bigint NOT NULL,

  txn_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL,

  work_order_id bigint NOT NULL,

  op_task_id bigint NOT NULL,

  txn_type varchar(32) NOT NULL,

  qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  from_status varchar(32) DEFAULT NULL,

  to_status varchar(32) NOT NULL,

  biz_type varchar(32) NOT NULL,

  biz_no varchar(64) NOT NULL,

  from_op_task_id bigint NOT NULL DEFAULT '0',

  to_op_task_id bigint NOT NULL DEFAULT '0',

  txn_time datetime NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_wip_txn_uk_mes_wip_txn_no_deleted (txn_no,deleted),

  UNIQUE KEY mes_wip_txn_uk_mes_wip_txn_idem_deleted (idempotency_key,deleted),

  KEY mes_wip_txn_idx_mes_wip_txn_op_time (op_task_id,txn_time),

  KEY mes_wip_txn_idx_mes_wip_txn_biz (biz_type,biz_no)

);

-- ----------------------------
-- mes_work_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS mes_work_order (
  id bigint NOT NULL,

  work_order_no varchar(64) NOT NULL,

  prod_order_id bigint NOT NULL,

  prod_order_no varchar(64) NOT NULL,

  plant_id bigint NOT NULL,

  workshop_id bigint NOT NULL,

  wc_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  planned_qty decimal(20,6) NOT NULL,

  completed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  planned_start_time datetime DEFAULT NULL,

  planned_end_time datetime DEFAULT NULL,

  actual_start_time datetime DEFAULT NULL,

  actual_end_time datetime DEFAULT NULL,

  kit_status varchar(32) NOT NULL DEFAULT 'UNCHECKED',

  status varchar(32) NOT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY mes_work_order_uk_mes_work_order_no_deleted (work_order_no,deleted),

  KEY mes_work_order_idx_mes_work_order_prod (prod_order_id,status),

  KEY mes_work_order_idx_mes_work_order_workshop (workshop_id,wc_id,status)

);

-- ----------------------------
-- system_dept
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_dept (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL DEFAULT '',

  parent_id bigint NOT NULL DEFAULT '0',

  sort int NOT NULL DEFAULT '0',

  leader_user_id bigint DEFAULT NULL,

  phone varchar(11) DEFAULT NULL,

  email varchar(50) DEFAULT NULL,

  status tinyint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_dict_data
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_dict_data (
  id bigint NOT NULL AUTO_INCREMENT,

  sort int NOT NULL DEFAULT '0',

  label varchar(100) NOT NULL DEFAULT '',

  value varchar(100) NOT NULL DEFAULT '',

  dict_type varchar(100) NOT NULL DEFAULT '',

  status tinyint NOT NULL DEFAULT '0',

  color_type varchar(100) DEFAULT '',

  css_class varchar(100) DEFAULT '',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_dict_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_dict_type (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(100) NOT NULL DEFAULT '',

  type varchar(100) NOT NULL DEFAULT '',

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  deleted_time datetime DEFAULT NULL,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_login_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_login_log (
  id bigint NOT NULL AUTO_INCREMENT,

  log_type bigint NOT NULL,

  trace_id varchar(64) NOT NULL DEFAULT '',

  user_id bigint NOT NULL DEFAULT '0',

  user_type tinyint NOT NULL DEFAULT '0',

  username varchar(50) NOT NULL DEFAULT '',

  result tinyint NOT NULL,

  user_ip varchar(50) NOT NULL,

  user_agent varchar(512) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_login_log_idx_username (username),

  KEY system_login_log_idx_create_time (create_time)

);

-- ----------------------------
-- system_mail_account
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_mail_account (
  id bigint NOT NULL AUTO_INCREMENT,

  mail varchar(255) NOT NULL,

  username varchar(255) NOT NULL,

  password varchar(255) NOT NULL,

  host varchar(255) NOT NULL,

  port int NOT NULL,

  ssl_enable BOOLEAN NOT NULL DEFAULT FALSE,

  starttls_enable BOOLEAN NOT NULL DEFAULT FALSE,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_mail_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_mail_log (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint DEFAULT NULL,

  user_type tinyint DEFAULT NULL,

  to_mails varchar(1024) NOT NULL,

  cc_mails varchar(1024) DEFAULT NULL,

  bcc_mails varchar(1024) DEFAULT NULL,

  account_id bigint NOT NULL,

  from_mail varchar(255) NOT NULL,

  template_id bigint NOT NULL,

  template_code varchar(63) NOT NULL,

  template_nickname varchar(255) DEFAULT NULL,

  template_title varchar(255) NOT NULL,

  template_content CLOB NOT NULL,

  template_params varchar(255) NOT NULL,

  send_status tinyint NOT NULL DEFAULT '0',

  send_time datetime DEFAULT NULL,

  send_message_id varchar(255) DEFAULT NULL,

  send_exception varchar(4096) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_mail_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_mail_template (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(63) NOT NULL,

  code varchar(63) NOT NULL,

  account_id bigint NOT NULL,

  nickname varchar(255) DEFAULT NULL,

  title varchar(255) NOT NULL,

  content varchar(10240) NOT NULL,

  params varchar(255) NOT NULL,

  status tinyint NOT NULL,

  remark varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_menu (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(50) NOT NULL,

  permission varchar(100) NOT NULL DEFAULT '',

  type tinyint NOT NULL,

  sort int NOT NULL DEFAULT '0',

  parent_id bigint NOT NULL DEFAULT '0',

  path varchar(200) DEFAULT '',

  icon varchar(100) DEFAULT '#',

  component varchar(255) DEFAULT NULL,

  component_name varchar(255) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  visible BOOLEAN NOT NULL DEFAULT TRUE,

  keep_alive BOOLEAN NOT NULL DEFAULT TRUE,

  always_show BOOLEAN NOT NULL DEFAULT TRUE,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_mobile_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_mobile_menu (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(50) NOT NULL,

  permission varchar(100) NOT NULL DEFAULT '',

  type tinyint NOT NULL,

  sort int NOT NULL DEFAULT '0',

  parent_id bigint NOT NULL DEFAULT '0',

  path varchar(200) DEFAULT '',

  icon varchar(100) DEFAULT '#',

  status tinyint NOT NULL DEFAULT '0',

  visible BOOLEAN NOT NULL DEFAULT TRUE,

  remark varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_mobile_role_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_mobile_role_menu (
  id bigint NOT NULL AUTO_INCREMENT,

  role_id bigint NOT NULL,

  menu_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY system_mobile_role_menu_uk_role_menu (role_id,menu_id)

);

-- ----------------------------
-- system_notice
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_notice (
  id bigint NOT NULL AUTO_INCREMENT,

  title varchar(50) NOT NULL,

  content CLOB NOT NULL,

  type tinyint NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_notify_message
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_notify_message (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL,

  template_id bigint NOT NULL,

  template_code varchar(64) NOT NULL,

  template_nickname varchar(63) NOT NULL,

  template_content CLOB NOT NULL,

  template_type int NOT NULL,

  template_params varchar(255) NOT NULL,

  read_status BOOLEAN NOT NULL,

  read_time datetime DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_notify_message_idx_user_id_user_type_read_status (user_id,user_type,read_status)

);

-- ----------------------------
-- system_notify_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_notify_template (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(63) NOT NULL,

  code varchar(64) NOT NULL,

  nickname varchar(255) NOT NULL,

  content CLOB NOT NULL,

  type tinyint NOT NULL,

  params varchar(255) DEFAULT NULL,

  status tinyint NOT NULL,

  remark varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_oauth2_access_token
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_oauth2_access_token (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL,

  user_info varchar(512) NOT NULL,

  access_token varchar(255) NOT NULL,

  refresh_token varchar(32) NOT NULL,

  client_id varchar(255) NOT NULL,

  scopes varchar(255) DEFAULT NULL,

  expires_time datetime NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_oauth2_access_token_idx_access_token (access_token),

  KEY system_oauth2_access_token_idx_refresh_token (refresh_token)

);

-- ----------------------------
-- system_oauth2_approve
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_oauth2_approve (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL,

  client_id varchar(255) NOT NULL,

  scope varchar(255) NOT NULL DEFAULT '',

  approved BOOLEAN NOT NULL DEFAULT FALSE,

  expires_time datetime NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_oauth2_approve_idx_user_id_user_type_client_id (user_id,user_type,client_id)

);

-- ----------------------------
-- system_oauth2_client
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_oauth2_client (
  id bigint NOT NULL AUTO_INCREMENT,

  client_id varchar(255) NOT NULL,

  secret varchar(255) NOT NULL,

  name varchar(255) NOT NULL,

  logo varchar(255) NOT NULL,

  description varchar(255) DEFAULT NULL,

  status tinyint NOT NULL,

  access_token_validity_seconds int NOT NULL,

  refresh_token_validity_seconds int NOT NULL,

  redirect_uris varchar(255) NOT NULL,

  authorized_grant_types varchar(255) NOT NULL,

  scopes varchar(255) DEFAULT NULL,

  auto_approve_scopes varchar(255) DEFAULT NULL,

  authorities varchar(255) DEFAULT NULL,

  resource_ids varchar(255) DEFAULT NULL,

  additional_information varchar(4096) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  KEY system_oauth2_client_idx_client_id (client_id)

);

-- ----------------------------
-- system_oauth2_code
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_oauth2_code (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL,

  code varchar(32) NOT NULL,

  client_id varchar(255) NOT NULL,

  scopes varchar(255) DEFAULT '',

  expires_time datetime NOT NULL,

  redirect_uri varchar(255) DEFAULT NULL,

  state varchar(255) NOT NULL DEFAULT '',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_oauth2_code_idx_code (code)

);

-- ----------------------------
-- system_oauth2_refresh_token
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_oauth2_refresh_token (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  refresh_token varchar(32) NOT NULL,

  user_type tinyint NOT NULL,

  client_id varchar(255) NOT NULL,

  scopes varchar(255) DEFAULT NULL,

  expires_time datetime NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_oauth2_refresh_token_idx_refresh_token (refresh_token)

);

-- ----------------------------
-- system_operate_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_operate_log (
  id bigint NOT NULL AUTO_INCREMENT,

  trace_id varchar(64) NOT NULL DEFAULT '',

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL DEFAULT '0',

  type varchar(50) NOT NULL,

  sub_type varchar(50) NOT NULL,

  biz_id bigint NOT NULL,

  action varchar(2000) NOT NULL DEFAULT '',

  success BOOLEAN NOT NULL DEFAULT TRUE,

  extra varchar(2000) NOT NULL DEFAULT '',

  request_method varchar(16) DEFAULT '',

  request_url varchar(255) DEFAULT '',

  user_ip varchar(50) DEFAULT NULL,

  user_agent varchar(512) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_operate_log_idx_user_id (user_id),

  KEY system_operate_log_idx_create_time (create_time)

);

-- ----------------------------
-- system_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_post (
  id bigint NOT NULL AUTO_INCREMENT,

  code varchar(64) NOT NULL,

  name varchar(50) NOT NULL,

  sort int NOT NULL,

  status tinyint NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_role (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL,

  code varchar(100) NOT NULL,

  sort int NOT NULL,

  data_scope tinyint NOT NULL DEFAULT '1',

  data_scope_dept_ids varchar(500) NOT NULL DEFAULT '',

  status tinyint NOT NULL,

  type tinyint NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_role_menu
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_role_menu (
  id bigint NOT NULL AUTO_INCREMENT,

  role_id bigint NOT NULL,

  menu_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_role_menu_idx_role_id (role_id)

);

-- ----------------------------
-- system_sms_channel
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_sms_channel (
  id bigint NOT NULL AUTO_INCREMENT,

  signature varchar(12) NOT NULL,

  code varchar(63) NOT NULL,

  status tinyint NOT NULL,

  remark varchar(255) DEFAULT NULL,

  api_key varchar(128) NOT NULL,

  api_secret varchar(128) DEFAULT NULL,

  callback_url varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_sms_code
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_sms_code (
  id bigint NOT NULL AUTO_INCREMENT,

  mobile varchar(11) NOT NULL,

  code varchar(6) NOT NULL,

  create_ip varchar(15) NOT NULL,

  scene tinyint NOT NULL,

  today_index tinyint NOT NULL,

  used tinyint NOT NULL,

  used_time datetime DEFAULT NULL,

  used_ip varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_sms_code_idx_mobile (mobile)

);

-- ----------------------------
-- system_sms_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_sms_log (
  id bigint NOT NULL AUTO_INCREMENT,

  channel_id bigint NOT NULL,

  channel_code varchar(63) NOT NULL,

  template_id bigint NOT NULL,

  template_code varchar(63) NOT NULL,

  template_type tinyint NOT NULL,

  template_content varchar(255) NOT NULL,

  template_params varchar(255) NOT NULL,

  api_template_id varchar(63) NOT NULL,

  mobile varchar(11) NOT NULL,

  user_id bigint DEFAULT NULL,

  user_type tinyint DEFAULT NULL,

  send_status tinyint NOT NULL DEFAULT '0',

  send_time datetime DEFAULT NULL,

  api_send_code varchar(63) DEFAULT NULL,

  api_send_msg varchar(255) DEFAULT NULL,

  api_request_id varchar(255) DEFAULT NULL,

  api_serial_no varchar(255) DEFAULT NULL,

  receive_status tinyint NOT NULL DEFAULT '0',

  receive_time datetime DEFAULT NULL,

  api_receive_code varchar(63) DEFAULT NULL,

  api_receive_msg varchar(255) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_sms_template
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_sms_template (
  id bigint NOT NULL AUTO_INCREMENT,

  type tinyint NOT NULL,

  status tinyint NOT NULL,

  code varchar(63) NOT NULL,

  name varchar(63) NOT NULL,

  content varchar(255) NOT NULL,

  params varchar(255) NOT NULL,

  remark varchar(255) DEFAULT NULL,

  api_template_id varchar(63) NOT NULL,

  channel_id bigint NOT NULL,

  channel_code varchar(63) NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_social_client
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_social_client (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(255) NOT NULL,

  social_type tinyint NOT NULL,

  user_type tinyint NOT NULL,

  client_id varchar(255) NOT NULL,

  client_secret varchar(255) NOT NULL,

  agent_id varchar(255) DEFAULT NULL,

  public_key varchar(2048) DEFAULT NULL,

  status tinyint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_social_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_social_user (
  id bigint NOT NULL AUTO_INCREMENT,

  type tinyint NOT NULL,

  openid varchar(32) NOT NULL,

  token varchar(256) DEFAULT NULL,

  raw_token_info varchar(1024) NOT NULL,

  nickname varchar(32) NOT NULL,

  avatar varchar(255) DEFAULT NULL,

  raw_user_info varchar(1024) NOT NULL,

  code varchar(256) NOT NULL,

  state varchar(256) DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_social_user_idx_type_openid (type,openid),

  KEY system_social_user_idx_type_code_state (type,code,state)

);

-- ----------------------------
-- system_social_user_bind
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_social_user_bind (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  user_type tinyint NOT NULL,

  social_type tinyint NOT NULL,

  social_user_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_social_user_bind_idx_user_type_social_user_id (user_type,social_user_id)

);

-- ----------------------------
-- system_tenant
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_tenant (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL,

  contact_user_id bigint DEFAULT NULL,

  contact_name varchar(30) NOT NULL,

  contact_mobile varchar(500) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  websites varchar(1024) DEFAULT '',

  package_id bigint NOT NULL,

  expire_time datetime NOT NULL,

  account_count int NOT NULL,

  creator varchar(64) NOT NULL DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_tenant_package
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_tenant_package (
  id bigint NOT NULL AUTO_INCREMENT,

  name varchar(30) NOT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(256) DEFAULT '',

  menu_ids varchar(4096) NOT NULL,

  creator varchar(64) NOT NULL DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_user_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_user_post (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL DEFAULT '0',

  post_id bigint NOT NULL DEFAULT '0',

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id)

);

-- ----------------------------
-- system_user_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_user_role (
  id bigint NOT NULL AUTO_INCREMENT,

  user_id bigint NOT NULL,

  role_id bigint NOT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_user_role_idx_user_id (user_id)

);

-- ----------------------------
-- system_users
-- ----------------------------
CREATE TABLE IF NOT EXISTS system_users (
  id bigint NOT NULL AUTO_INCREMENT,

  username varchar(30) NOT NULL,

  password varchar(100) NOT NULL DEFAULT '',

  nickname varchar(30) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  dept_id bigint DEFAULT NULL,

  post_ids varchar(255) DEFAULT NULL,

  email varchar(50) DEFAULT '',

  mobile varchar(11) DEFAULT '',

  sex tinyint DEFAULT '0',

  avatar varchar(512) DEFAULT '',

  status tinyint NOT NULL DEFAULT '0',

  login_ip varchar(50) DEFAULT '',

  login_date datetime DEFAULT NULL,

  creator varchar(64) DEFAULT '',

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT '',

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  tenant_id bigint NOT NULL DEFAULT '0',

  PRIMARY KEY (id),

  KEY system_users_idx_username (username),

  KEY system_users_idx_mobile (mobile),

  KEY system_users_idx_email (email),

  KEY system_users_idx_dept_id (dept_id)

);

-- ----------------------------
-- wms_arrival
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_arrival (
  id bigint NOT NULL,

  arrival_no varchar(64) NOT NULL,

  source_type varchar(32) NOT NULL,

  source_no varchar(64) NOT NULL,

  supplier_id bigint DEFAULT NULL,

  supplier_code varchar(64) DEFAULT NULL,

  supplier_name varchar(128) DEFAULT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  expect_date date DEFAULT NULL,

  arrival_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_arrival_uk_wms_arrival_no_deleted (arrival_no,deleted),

  KEY wms_arrival_idx_wms_arrival_source (source_type,source_no),

  KEY wms_arrival_idx_wms_arrival_supplier_date (supplier_id,arrival_time)

);

-- ----------------------------
-- wms_arrival_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_arrival_line (
  id bigint NOT NULL,

  arrival_id bigint NOT NULL,

  line_no int NOT NULL,

  source_line_no varchar(64) DEFAULT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  lot_no varchar(128) DEFAULT NULL,

  supplier_lot_no varchar(128) DEFAULT NULL,

  arrival_qty decimal(20,6) NOT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  need_qc BOOLEAN NOT NULL DEFAULT FALSE,

  iqc_mode varchar(32) DEFAULT NULL,

  qc_task_id bigint DEFAULT NULL,

  qualified_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  rejected_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_arrival_line_uk_wms_arrival_line_no_deleted (arrival_id,line_no,deleted),

  KEY wms_arrival_line_idx_wms_arrival_line_mat (mat_id,lot_no),

  KEY wms_arrival_line_idx_wms_arrival_line_qc_task (qc_task_id)

);

-- ----------------------------
-- wms_batch
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_batch (
  id bigint NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  lot_no varchar(128) NOT NULL,

  sn_no varchar(128) DEFAULT NULL,

  supplier_id bigint DEFAULT NULL,

  supplier_code varchar(64) DEFAULT NULL,

  supplier_lot_no varchar(128) DEFAULT NULL,

  furnace_lot_no varchar(128) DEFAULT NULL,

  produce_date date DEFAULT NULL,

  expiry_date date DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_batch_uk_wms_batch_mat_lot_deleted (mat_id,lot_no,deleted),

  UNIQUE KEY wms_batch_uk_wms_batch_sn_deleted (sn_no,deleted),

  KEY wms_batch_idx_wms_batch_expiry_date (expiry_date),

  KEY wms_batch_idx_wms_batch_supplier_id (supplier_id)

);

-- ----------------------------
-- wms_command_record
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_command_record (
  id bigint NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  command_type varchar(64) NOT NULL,

  source_module varchar(32) NOT NULL,

  source_biz_no varchar(64) NOT NULL,

  source_biz_line_no varchar(64) DEFAULT NULL,

  result_id bigint DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_command_record_uk_wms_command_idempotency_deleted (idempotency_key,deleted)

);

-- ----------------------------
-- wms_container
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_container (
  id bigint NOT NULL,

  container_no varchar(128) NOT NULL,

  type_id bigint NOT NULL,

  inv_org_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  loc_id bigint NOT NULL DEFAULT '0',

  length decimal(20,6) DEFAULT NULL,

  width decimal(20,6) DEFAULT NULL,

  height decimal(20,6) DEFAULT NULL,

  gross_weight decimal(20,6) DEFAULT NULL,

  status varchar(32) NOT NULL DEFAULT 'EMPTY',

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_container_uk_wms_container_no_deleted (container_no,deleted),

  KEY wms_container_idx_wms_container_loc_status (loc_id,status),

  KEY wms_container_idx_wms_container_wh_id (wh_id)

);

-- ----------------------------
-- wms_container_type
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_container_type (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  std_length decimal(20,6) DEFAULT NULL,

  std_width decimal(20,6) DEFAULT NULL,

  std_height decimal(20,6) DEFAULT NULL,

  max_weight decimal(20,6) DEFAULT NULL,

  max_volume decimal(20,6) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_container_type_uk_wms_container_type_code_deleted (code,deleted)

);

-- ----------------------------
-- wms_count_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_count_order (
  id bigint NOT NULL,

  count_no varchar(64) NOT NULL,

  count_type varchar(32) NOT NULL,

  inv_org_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  zone_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  snapshot_time datetime NOT NULL,

  blind_count_flag BOOLEAN NOT NULL DEFAULT FALSE,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  approval_status int NOT NULL DEFAULT '-1',

  process_instance_id varchar(64) DEFAULT NULL,

  approval_time datetime DEFAULT NULL,

  approval_reason varchar(500) DEFAULT NULL,

  PRIMARY KEY (id),

  UNIQUE KEY wms_count_order_uk_wms_count_order_no_deleted (count_no,deleted),

  UNIQUE KEY wms_count_order_uk_wms_count_order_idem_deleted (idempotency_key,deleted),

  UNIQUE KEY wms_count_order_uk_process_instance_id (process_instance_id),

  KEY wms_count_order_idx_wms_count_order_wh_status (wh_id,status),

  KEY wms_count_order_idx_wms_count_order_snapshot (snapshot_time)

);

-- ----------------------------
-- wms_count_order_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_count_order_line (
  id bigint NOT NULL,

  order_id bigint NOT NULL,

  line_no int NOT NULL,

  balance_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  batch_id bigint NOT NULL,

  lot_no varchar(128) NOT NULL,

  loc_id bigint NOT NULL,

  loc_code varchar(64) NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  quality_status varchar(32) NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  book_qty decimal(20,6) NOT NULL,

  actual_qty decimal(20,6) DEFAULT NULL,

  diff_qty decimal(20,6) DEFAULT NULL,

  adjust_biz_no varchar(64) DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_count_order_line_uk_wms_count_line_balance_deleted (order_id,balance_id,deleted),

  UNIQUE KEY wms_count_order_line_uk_wms_count_line_no_deleted (order_id,line_no,deleted),

  KEY wms_count_order_line_idx_wms_count_line_loc (loc_id,status),

  KEY wms_count_order_line_idx_wms_count_line_mat (mat_id,batch_id)

);

-- ----------------------------
-- wms_device_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_device_task (
  id bigint NOT NULL,

  task_no varchar(64) NOT NULL,

  external_task_no varchar(128) DEFAULT NULL,

  task_type varchar(32) NOT NULL,

  device_type varchar(32) NOT NULL,

  source_biz_type varchar(32) NOT NULL,

  source_biz_no varchar(64) NOT NULL,

  source_line_id bigint DEFAULT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  from_loc_id bigint NOT NULL DEFAULT '0',

  to_loc_id bigint NOT NULL DEFAULT '0',

  actual_loc_id bigint NOT NULL DEFAULT '0',

  external_system_code varchar(64) NOT NULL,

  dispatch_status varchar(32) NOT NULL,

  status varchar(32) NOT NULL,

  post_status varchar(32) NOT NULL,

  request_snapshot_json CLOB DEFAULT NULL,

  callback_snapshot_json CLOB DEFAULT NULL,

  idempotency_key varchar(128) NOT NULL,

  completed_time datetime DEFAULT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_device_task_uk_wms_device_task_no_deleted (task_no,deleted),

  UNIQUE KEY wms_device_task_uk_wms_device_task_idem_deleted (idempotency_key,deleted),

  UNIQUE KEY wms_device_task_uk_wms_device_external_deleted (external_system_code,external_task_no,deleted),

  KEY wms_device_task_idx_wms_device_task_source (source_biz_type,source_biz_no),

  KEY wms_device_task_idx_wms_device_task_status (device_type,status,dispatch_status)

);

-- ----------------------------
-- wms_in_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_in_order (
  id bigint NOT NULL,

  in_no varchar(64) NOT NULL,

  biz_type varchar(32) NOT NULL,

  source_type varchar(32) DEFAULT NULL,

  source_biz_no varchar(64) DEFAULT NULL,

  source_line_id bigint DEFAULT NULL,

  origin_type varchar(32) DEFAULT NULL,

  origin_no varchar(64) DEFAULT NULL,

  arrival_id bigint DEFAULT NULL,

  supplier_id bigint DEFAULT NULL,

  customer_id bigint DEFAULT NULL,

  prod_order_no varchar(64) DEFAULT NULL,

  work_order_no varchar(64) DEFAULT NULL,

  transfer_no varchar(64) DEFAULT NULL,

  count_order_no varchar(64) DEFAULT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  wh_id bigint NOT NULL,

  wh_code varchar(64) NOT NULL,

  from_inv_org_id bigint NOT NULL DEFAULT '0',

  from_wh_id bigint NOT NULL DEFAULT '0',

  need_qc BOOLEAN NOT NULL DEFAULT FALSE,

  iqc_mode varchar(32) DEFAULT NULL,

  quality_status varchar(32) NOT NULL DEFAULT 'QUALIFIED',

  hook_mode varchar(32) NOT NULL DEFAULT 'NONE',

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  biz_attrs_json CLOB DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_in_order_uk_wms_in_order_no_deleted (in_no,deleted),

  UNIQUE KEY wms_in_order_uk_wms_in_order_idempotency_deleted (idempotency_key,deleted),

  KEY wms_in_order_idx_wms_in_order_source (source_type,source_biz_no),

  KEY wms_in_order_idx_wms_in_order_wh_status (wh_id,status),

  KEY wms_in_order_idx_wms_in_order_prod_order (prod_order_no)

);

-- ----------------------------
-- wms_in_order_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_in_order_line (
  id bigint NOT NULL,

  order_id bigint NOT NULL,

  line_no int NOT NULL,

  source_line_id bigint DEFAULT NULL,

  source_line_no varchar(64) DEFAULT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  batch_id bigint DEFAULT NULL,

  lot_no varchar(128) DEFAULT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  loc_id bigint DEFAULT NULL,

  loc_code varchar(64) DEFAULT NULL,

  quality_status varchar(32) NOT NULL,

  plan_qty decimal(20,6) NOT NULL,

  completed_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  posted_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  container_id bigint DEFAULT NULL,

  qc_task_id bigint DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_in_order_line_uk_wms_in_order_line_no_deleted (order_id,line_no,deleted),

  KEY wms_in_order_line_idx_wms_in_order_line_mat (mat_id,batch_id),

  KEY wms_in_order_line_idx_wms_in_order_line_qc_task (qc_task_id)

);

-- ----------------------------
-- wms_inv_balance
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_inv_balance (
  id bigint NOT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  loc_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  batch_id bigint NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  stock_scope varchar(32) NOT NULL DEFAULT 'COMMON',

  quality_status varchar(32) NOT NULL,

  qty_onhand decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_reserved decimal(20,6) NOT NULL DEFAULT '0.000000',

  qty_frozen decimal(20,6) NOT NULL DEFAULT '0.000000',

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  uom_code varchar(64) NOT NULL,

  lot_no varchar(128) NOT NULL,

  wh_code varchar(64) NOT NULL,

  loc_code varchar(64) NOT NULL,

  version int NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_inv_balance_uk_wms_inv_balance_dimension (inv_org_id,plant_id,wh_id,loc_id,mat_id,batch_id,container_id,project_id,project_task_id,stock_scope,quality_status,deleted),

  KEY wms_inv_balance_idx_wms_inv_balance_mat_available (inv_org_id,mat_id,quality_status,wh_id),

  KEY wms_inv_balance_idx_wms_inv_balance_project (project_id,project_task_id,mat_id),

  KEY wms_inv_balance_idx_wms_inv_balance_container (container_id)

);

-- ----------------------------
-- wms_inv_lock
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_inv_lock (
  id bigint NOT NULL,

  lock_no varchar(64) NOT NULL,

  biz_type varchar(32) NOT NULL,

  biz_no varchar(64) NOT NULL,

  inv_org_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  loc_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  batch_id bigint NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  quality_status varchar(32) NOT NULL,

  qty decimal(20,6) NOT NULL,

  released_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  reason varchar(500) NOT NULL,

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_inv_lock_uk_wms_inv_lock_no_deleted (lock_no,deleted),

  UNIQUE KEY wms_inv_lock_uk_wms_inv_lock_idempotency_deleted (idempotency_key,deleted),

  KEY wms_inv_lock_idx_wms_inv_lock_biz (biz_type,biz_no),

  KEY wms_inv_lock_idx_wms_inv_lock_balance (inv_org_id,wh_id,loc_id,mat_id,batch_id,status)

);

-- ----------------------------
-- wms_inv_reservation
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_inv_reservation (
  id bigint NOT NULL,

  reservation_no varchar(64) NOT NULL,

  biz_type varchar(32) NOT NULL,

  biz_no varchar(64) NOT NULL,

  biz_line_no varchar(64) DEFAULT NULL,

  inv_org_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  loc_id bigint NOT NULL,

  mat_id bigint NOT NULL,

  batch_id bigint NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  quality_status varchar(32) NOT NULL,

  qty decimal(20,6) NOT NULL,

  released_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  status varchar(32) NOT NULL,

  expire_time datetime DEFAULT NULL,

  idempotency_key varchar(128) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_inv_reservation_uk_wms_inv_reservation_no_deleted (reservation_no,deleted),

  UNIQUE KEY wms_inv_reservation_uk_wms_inv_reservation_idempotency_deleted (idempotency_key,deleted),

  KEY wms_inv_reservation_idx_wms_inv_reservation_biz (biz_type,biz_no),

  KEY wms_inv_reservation_idx_wms_inv_reservation_balance (inv_org_id,wh_id,loc_id,mat_id,batch_id,status)

);

-- ----------------------------
-- wms_inv_txn
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_inv_txn (
  id bigint NOT NULL,

  txn_no varchar(64) NOT NULL,

  txn_type varchar(32) NOT NULL,

  biz_type varchar(32) NOT NULL,

  biz_no varchar(64) NOT NULL,

  biz_line_no varchar(64) DEFAULT NULL,

  io_type varchar(16) NOT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  wh_id bigint NOT NULL,

  from_loc_id bigint NOT NULL DEFAULT '0',

  to_loc_id bigint NOT NULL DEFAULT '0',

  mat_id bigint NOT NULL,

  batch_id bigint NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  uom_id bigint NOT NULL,

  qty decimal(20,6) NOT NULL,

  before_qty decimal(20,6) NOT NULL,

  after_qty decimal(20,6) NOT NULL,

  quality_status_from varchar(32) DEFAULT NULL,

  quality_status_to varchar(32) DEFAULT NULL,

  project_id_from bigint NOT NULL DEFAULT '0',

  project_task_id_from bigint NOT NULL DEFAULT '0',

  project_id_to bigint NOT NULL DEFAULT '0',

  project_task_id_to bigint NOT NULL DEFAULT '0',

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  uom_code varchar(64) NOT NULL,

  lot_no varchar(128) NOT NULL,

  from_loc_code varchar(64) DEFAULT NULL,

  to_loc_code varchar(64) DEFAULT NULL,

  idempotency_key varchar(128) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_inv_txn_uk_wms_inv_txn_no_deleted (txn_no,deleted),

  UNIQUE KEY wms_inv_txn_uk_wms_inv_txn_idempotency_deleted (idempotency_key,deleted),

  KEY wms_inv_txn_idx_wms_inv_txn_biz (biz_type,biz_no),

  KEY wms_inv_txn_idx_wms_inv_txn_mat_time (mat_id,create_time),

  KEY wms_inv_txn_idx_wms_inv_txn_batch_id (batch_id)

);

-- ----------------------------
-- wms_loc
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_loc (
  id bigint NOT NULL,

  wh_id bigint NOT NULL,

  zone_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  loc_type varchar(32) NOT NULL,

  aisle_no varchar(32) DEFAULT NULL,

  row_no varchar(32) DEFAULT NULL,

  col_no varchar(32) DEFAULT NULL,

  level_no varchar(32) DEFAULT NULL,

  depth_no int DEFAULT NULL,

  max_length decimal(20,6) DEFAULT NULL,

  max_width decimal(20,6) DEFAULT NULL,

  max_height decimal(20,6) DEFAULT NULL,

  max_weight decimal(20,6) DEFAULT NULL,

  max_volume decimal(20,6) DEFAULT NULL,

  max_container_qty int DEFAULT NULL,

  dimension_check_mode varchar(32) NOT NULL DEFAULT 'NONE',

  status varchar(32) NOT NULL DEFAULT 'EMPTY',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_loc_uk_wms_loc_wh_code_deleted (wh_id,code,deleted),

  KEY wms_loc_idx_wms_loc_zone_status (zone_id,status)

);

-- ----------------------------
-- wms_out_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_out_order (
  id bigint NOT NULL,

  out_no varchar(64) NOT NULL,

  biz_type varchar(32) NOT NULL,

  source_type varchar(32) DEFAULT NULL,

  source_biz_no varchar(64) DEFAULT NULL,

  source_line_id bigint DEFAULT NULL,

  origin_type varchar(32) DEFAULT NULL,

  origin_no varchar(64) DEFAULT NULL,

  customer_id bigint DEFAULT NULL,

  supplier_id bigint DEFAULT NULL,

  prod_order_no varchar(64) DEFAULT NULL,

  work_order_no varchar(64) DEFAULT NULL,

  op_task_id bigint DEFAULT NULL,

  material_req_no varchar(64) DEFAULT NULL,

  transfer_no varchar(64) DEFAULT NULL,

  count_order_no varchar(64) DEFAULT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  project_id bigint NOT NULL DEFAULT '0',

  project_task_id bigint NOT NULL DEFAULT '0',

  wh_id bigint NOT NULL,

  wh_code varchar(64) NOT NULL,

  to_inv_org_id bigint NOT NULL DEFAULT '0',

  to_wh_id bigint NOT NULL DEFAULT '0',

  pick_strategy_id bigint DEFAULT NULL,

  oqc_task_id bigint DEFAULT NULL,

  hook_mode varchar(32) NOT NULL DEFAULT 'NONE',

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  biz_attrs_json CLOB DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_out_order_uk_wms_out_order_no_deleted (out_no,deleted),

  UNIQUE KEY wms_out_order_uk_wms_out_order_idempotency_deleted (idempotency_key,deleted),

  KEY wms_out_order_idx_wms_out_order_source (source_type,source_biz_no),

  KEY wms_out_order_idx_wms_out_order_wh_status (wh_id,status),

  KEY wms_out_order_idx_wms_out_order_prod_order (prod_order_no)

);

-- ----------------------------
-- wms_out_order_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_out_order_line (
  id bigint NOT NULL,

  order_id bigint NOT NULL,

  line_no int NOT NULL,

  source_line_id bigint DEFAULT NULL,

  source_line_no varchar(64) DEFAULT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  mat_spec varchar(255) DEFAULT NULL,

  batch_id bigint DEFAULT NULL,

  lot_no varchar(128) DEFAULT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  from_loc_id bigint DEFAULT NULL,

  from_loc_code varchar(64) DEFAULT NULL,

  to_loc_id bigint DEFAULT NULL,

  quality_status varchar(32) NOT NULL DEFAULT 'QUALIFIED',

  plan_qty decimal(20,6) NOT NULL,

  allocated_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  picked_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  posted_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  container_id bigint DEFAULT NULL,

  reservation_id bigint NOT NULL DEFAULT '0',

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_out_order_line_uk_wms_out_order_line_no_deleted (order_id,line_no,deleted),

  KEY wms_out_order_line_idx_wms_out_order_line_mat (mat_id,batch_id),

  KEY wms_out_order_line_idx_wms_out_order_line_loc (from_loc_id,status)

);

-- ----------------------------
-- wms_strategy
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_strategy (
  id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  strategy_type varchar(32) NOT NULL,

  wh_id bigint NOT NULL DEFAULT '0',

  version_no varchar(32) NOT NULL,

  config_json CLOB NOT NULL,

  priority int NOT NULL DEFAULT '0',

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_strategy_uk_wms_strategy_code_ver_deleted (strategy_type,code,version_no,deleted),

  KEY wms_strategy_idx_wms_strategy_match (strategy_type,wh_id,status,priority)

);

-- ----------------------------
-- wms_transfer_order
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_transfer_order (
  id bigint NOT NULL,

  transfer_no varchar(64) NOT NULL,

  transfer_type varchar(32) NOT NULL,

  source_type varchar(32) DEFAULT NULL,

  source_biz_no varchar(64) DEFAULT NULL,

  from_inv_org_id bigint NOT NULL,

  to_inv_org_id bigint NOT NULL,

  from_wh_id bigint NOT NULL,

  to_wh_id bigint NOT NULL,

  from_project_id bigint NOT NULL DEFAULT '0',

  to_project_id bigint NOT NULL DEFAULT '0',

  hook_mode varchar(32) NOT NULL DEFAULT 'NONE',

  status varchar(32) NOT NULL,

  idempotency_key varchar(128) DEFAULT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_transfer_order_uk_wms_transfer_order_no_deleted (transfer_no,deleted),

  UNIQUE KEY wms_transfer_order_uk_wms_transfer_order_idem_deleted (idempotency_key,deleted),

  KEY wms_transfer_order_idx_wms_transfer_order_status (transfer_type,status),

  KEY wms_transfer_order_idx_wms_transfer_order_source (source_type,source_biz_no)

);

-- ----------------------------
-- wms_transfer_order_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_transfer_order_line (
  id bigint NOT NULL,

  order_id bigint NOT NULL,

  line_no int NOT NULL,

  mat_id bigint NOT NULL,

  mat_code varchar(64) NOT NULL,

  mat_name varchar(128) NOT NULL,

  batch_id bigint NOT NULL,

  lot_no varchar(128) NOT NULL,

  container_id bigint NOT NULL DEFAULT '0',

  from_loc_id bigint NOT NULL DEFAULT '0',

  to_loc_id bigint NOT NULL DEFAULT '0',

  from_stock_scope varchar(32) NOT NULL DEFAULT 'COMMON',

  to_stock_scope varchar(32) NOT NULL DEFAULT 'COMMON',

  from_project_id bigint NOT NULL DEFAULT '0',

  to_project_id bigint NOT NULL DEFAULT '0',

  from_project_task_id bigint NOT NULL DEFAULT '0',

  to_project_task_id bigint NOT NULL DEFAULT '0',

  quality_status_from varchar(32) NOT NULL,

  quality_status_to varchar(32) NOT NULL,

  uom_id bigint NOT NULL,

  uom_code varchar(64) NOT NULL,

  plan_qty decimal(20,6) NOT NULL,

  moved_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  posted_qty decimal(20,6) NOT NULL DEFAULT '0.000000',

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_transfer_order_line_uk_wms_transfer_line_no_deleted (order_id,line_no,deleted),

  KEY wms_transfer_order_line_idx_wms_transfer_line_mat (mat_id,batch_id),

  KEY wms_transfer_order_line_idx_wms_transfer_line_locs (from_loc_id,to_loc_id)

);

-- ----------------------------
-- wms_wave
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_wave (
  id bigint NOT NULL,

  wave_no varchar(64) NOT NULL,

  wh_id bigint NOT NULL,

  wave_type varchar(32) NOT NULL,

  plan_start_time datetime DEFAULT NULL,

  plan_end_time datetime DEFAULT NULL,

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_wave_uk_wms_wave_no_deleted (wave_no,deleted),

  KEY wms_wave_idx_wms_wave_wh_status (wh_id,status)

);

-- ----------------------------
-- wms_wave_line
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_wave_line (
  id bigint NOT NULL,

  wave_id bigint NOT NULL,

  line_no int NOT NULL,

  out_order_id bigint NOT NULL,

  out_order_line_id bigint NOT NULL,

  priority int NOT NULL DEFAULT '0',

  status varchar(32) NOT NULL,

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_wave_line_uk_wms_wave_line_order_deleted (wave_id,out_order_line_id,deleted),

  UNIQUE KEY wms_wave_line_uk_wms_wave_line_no_deleted (wave_id,line_no,deleted),

  KEY wms_wave_line_idx_wms_wave_line_out_order (out_order_id,status)

);

-- ----------------------------
-- wms_wh
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_wh (
  id bigint NOT NULL,

  inv_org_id bigint NOT NULL,

  plant_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  wh_type varchar(32) NOT NULL,

  managed_by varchar(32) NOT NULL DEFAULT 'MANUAL',

  external_system_code varchar(64) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_wh_uk_wms_wh_org_code_deleted (inv_org_id,code,deleted),

  KEY wms_wh_idx_wms_wh_plant_id (plant_id)

);

-- ----------------------------
-- wms_zone
-- ----------------------------
CREATE TABLE IF NOT EXISTS wms_zone (
  id bigint NOT NULL,

  wh_id bigint NOT NULL,

  code varchar(64) NOT NULL,

  name varchar(128) NOT NULL,

  zone_type varchar(32) NOT NULL,

  allow_mixed_mat BOOLEAN NOT NULL DEFAULT FALSE,

  allow_mixed_lot BOOLEAN NOT NULL DEFAULT FALSE,

  temp_range varchar(64) DEFAULT NULL,

  status tinyint NOT NULL DEFAULT '0',

  remark varchar(500) DEFAULT NULL,

  creator varchar(64) DEFAULT NULL,

  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  updater varchar(64) DEFAULT NULL,

  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,

  deleted BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (id),

  UNIQUE KEY wms_zone_uk_wms_zone_wh_code_deleted (wh_id,code,deleted),

  KEY wms_zone_idx_wms_zone_type (zone_type)

);

-- ----------------------------
-- @KeySequence 序列（MyBatis-Plus H2KeyGenerator 依赖）
-- ----------------------------
CREATE SEQUENCE IF NOT EXISTS bpm_category_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_form_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_oa_leave_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_definition_info_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_expression_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_instance_copy_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_listener_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_user_group_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS iboot_demo01_contact_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS iboot_demo02_category_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS iboot_demo03_course_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS iboot_demo03_grade_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS iboot_demo03_student_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_code_rule_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_code_sequence_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_codegen_column_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_codegen_table_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_config_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_data_source_config_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_file_config_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_file_content_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_file_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_job_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS infra_job_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_defect_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_dispatch_task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_disposition_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_kit_snapshot_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_material_req_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_material_req_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_op_skip_record_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_op_task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_prod_demand_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_prod_order_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_prod_report_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_qc_item_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_qc_plan_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_qc_result_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_qc_rule_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_qc_task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_rework_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_rgv_task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_wip_txn_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS mes_work_order_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_dept_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_dict_data_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_dict_type_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_login_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_mail_account_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_mail_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_mail_template_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_menu_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_notice_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_notify_message_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_notify_template_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_oauth2_access_token_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_oauth2_approve_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_oauth2_client_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_oauth2_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_operate_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_post_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_role_menu_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_sms_channel_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_sms_code_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_sms_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_sms_template_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_social_client_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_social_user_bind_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_social_user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_tenant_package_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_tenant_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_user_post_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_user_role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS system_users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_arrival_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_arrival_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_device_task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_strategy_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_wave_line_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS wms_wave_seq START WITH 1 INCREMENT BY 1;
