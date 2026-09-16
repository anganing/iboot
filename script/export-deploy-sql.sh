#!/bin/bash
#
# T5-04 部署交付脚本导出器
#
# 作用：从开发库导出台 MOM 改造交付所需的「表结构 + 配置数据」，产出 sql/deploy/*.sql。
# 特点：可重复执行（连跑两次结果一致）、不手工拼 SQL（数据段全部由 mysqldump 生成）。
#
# 用法：
#   bash script/export-deploy-sql.sh
#   MYSQL_HOST=127.0.0.1 MYSQL_PWD=xxx bash script/export-deploy-sql.sh
#
# 凭据来源（按优先级，脚本内不硬编码密码）：
#   1. 环境变量 MYSQL_HOST / MYSQL_PORT / MYSQL_USER / MYSQL_PWD / MYSQL_DATABASE
#   2. 同目录下的 .env.deploy（存在则 source；该文件不入库）
#   3. 兜底默认值（仅开发机通用口径）
#
# mysqldump 通道：
#   1. 本机 PATH 里的 mysqldump
#   2. 本机没有时，若存在容器 $MYSQL_DOCKER_CONTAINER，则 docker exec 进容器执行
#      （开发机 MySQL 跑在 Docker 里，镜像自带 mysqldump，这是本项目的实际形态）

set -euo pipefail

# ---------------------------------------------------------------- 路径

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="$PROJECT_ROOT/sql/deploy"

# ---------------------------------------------------------------- 凭据

[ -f "$SCRIPT_DIR/.env.deploy" ] && source "$SCRIPT_DIR/.env.deploy"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PWD="${MYSQL_PWD:-123456}"
MYSQL_DATABASE="${MYSQL_DATABASE:-ruoyi-vue-pro-20260817}"
MYSQL_DOCKER_CONTAINER="${MYSQL_DOCKER_CONTAINER:-mysql-server}"

export MYSQL_PWD

# ---------------------------------------------------------------- mysqldump 封装

DUMP_MODE=""

detect_dump_mode() {
  if command -v mysqldump >/dev/null 2>&1; then
    DUMP_MODE="local"
  elif command -v docker >/dev/null 2>&1 \
       && docker ps --format '{{.Names}}' 2>/dev/null | grep -qx "$MYSQL_DOCKER_CONTAINER"; then
    DUMP_MODE="docker"
  else
    echo "[ERROR] 找不到 mysqldump：本机 PATH 没有，也没有可用容器 '$MYSQL_DOCKER_CONTAINER'" >&2
    echo "        请安装 mysql-client，或用 MYSQL_DOCKER_CONTAINER=<容器名> 指定 MySQL 容器" >&2
    exit 1
  fi
  echo "[INFO] mysqldump 通道：$DUMP_MODE"
}

# 统一出口。凭据走 MYSQL_PWD 环境变量，不出现在命令行里。
#
# 参数顺序（实测 MySQL 8.0.32 的真实行为，不是文档口径）：
#   导数据时最后一个位置参数被当作库名，所以必须写成「db 表1 表2 ...」，
#   把库名写在表名前面。反过来写成「表名 db」会报
#   `Unknown database '<表名>'`。
#   导结构（--no-data）时不需要位置参数，库名放哪都对。
#   本函数把库名插在选项之后、表名之前，两种场景都成立。
run_dump() {
  local opts=()
  if [ "$DUMP_MODE" = "local" ]; then
    opts=(-h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER")
  else
    opts=(-i -e MYSQL_PWD="$MYSQL_PWD" "$MYSQL_DOCKER_CONTAINER")
  fi

  local dump_cmd=(mysqldump -u"$MYSQL_USER" --set-gtid-purged=OFF --skip-comments)
  # $@ 里前段是 mysqldump 选项、后段是表名。选项一定以 '-' 开头，
  # 据此把两者分开，好在中间插入库名。
  local args=() tables=() seen_table=0
  local a
  for a in "$@"; do
    if [ "$seen_table" -eq 0 ] && [[ "$a" == -* ]]; then
      args+=("$a")
    else
      seen_table=1
      tables+=("$a")
    fi
  done

  if [ "$DUMP_MODE" = "local" ]; then
    mysqldump "${opts[@]}" "${dump_cmd[@]:1}" "${args[@]}" "$MYSQL_DATABASE" ${tables[@]+"${tables[@]}"}
  else
    docker exec "${opts[@]}" "${dump_cmd[@]}" "${args[@]}" "$MYSQL_DATABASE" ${tables[@]+"${tables[@]}"}
  fi
}

# 导出数据文件时需要「选项 → 表名 → 库名」的顺序，这里做一层显式封装，
# 避免调用点把选项写在表名后面（mysqldump 会把选项当成表名解析）。
run_dump_data() {
  run_dump "${DUMP_DATA_ARGS[@]}" "$@"
}

# 只导结构
DUMP_SCHEMA_ARGS=(--no-data --skip-add-drop-table)

# 只导数据。
#   --skip-extended-insert  一行一条 INSERT：便于 diff 与人工核对，也让输出稳定
#   --order-by-primary      按主键排序，是无 ORDER BY 时结果稳定的前提
DUMP_DATA_ARGS=(--no-create-info --skip-extended-insert --order-by-primary)

# ---------------------------------------------------------------- 文件头尾

# $1=文件 $2=该文件作用说明
write_header() {
  cat > "$1" <<EOF
-- ============================================================================
-- iboot MOM 交付脚本 —— $(basename "$1")
-- $2
--
-- 本文件由 script/export-deploy-sql.sh 自动生成，请勿手工编辑。
-- 需要调整内容请修改导出脚本后重新执行，否则下次导出会被覆盖。
--
-- 导出源：$MYSQL_DATABASE @ $MYSQL_HOST:$MYSQL_PORT
-- 导出时间：$(date '+%Y-%m-%d %H:%M:%S')
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

EOF
}

write_footer() {
  cat >> "$1" <<'EOF'

SET FOREIGN_KEY_CHECKS = 1;
-- ============================ 文件结束 ======================================
EOF
}

# 生成一个「数据文件」：头 + mysqldump 数据段 + 尾。
# $1=输出文件  $2=说明（其余参数）=表名
export_data_file() {
  local out="$1" desc="$2"; shift 2
  local tmp="$out.new"
  if [ "$#" -gt 0 ]; then
    { write_header "$out" "$desc"; run_dump_data "$@"; write_footer "$out"; } > "$tmp"
  else
    { write_header "$out" "$desc"; run_dump_data; write_footer "$out"; } > "$tmp"
  fi
  mv "$tmp" "$out"
  echo "     $(basename "$out")  $(wc -l < "$out") 行"
}

# ---------------------------------------------------------------- 主流程

detect_dump_mode
mkdir -p "$OUT_DIR"
echo "[INFO] 源库：$MYSQL_DATABASE @ $MYSQL_HOST:$MYSQL_PORT"
echo "[INFO] 输出：$OUT_DIR"

# ---- 01-schema.sql：全量表结构 ----
# 走「一次导全库 + awk 剔除引擎表」而不是逐表循环：表多时快得多，也不会漏表。
# 剔除 ACT_* / FLW_*：Flowable 与 FlowLong 的表由引擎启动时自建，交付脚本不含其结构。
echo "[1/10] 01-schema.sql 全量表结构"
{
  write_header "$OUT_DIR/01-schema.sql" \
    "全量表结构 DDL（不含数据；不含 ACT_* 与 FLW_*，流程引擎启动时自建）"
  run_dump --no-data --skip-add-drop-table \
    | awk '
      /^CREATE TABLE `(ACT_|FLW_)/ { skip = 1 }
      skip && /^\) ENGINE/          { skip = 0; next }
      skip                          { next }
      NF || prev_nf                 { print }
      { prev_nf = NF }
    '
  write_footer "$OUT_DIR/01-schema.sql"
} > "$OUT_DIR/01-schema.sql.new"
mv "$OUT_DIR/01-schema.sql.new" "$OUT_DIR/01-schema.sql"
echo "     01-schema.sql  $(wc -l < "$OUT_DIR/01-schema.sql") 行"

# ---- 02~09：配置数据 ----
echo "[2/10] 02-menu.sql"
export_data_file "$OUT_DIR/02-menu.sql" \
  "菜单与按钮权限（system_menu 全量，含 MOM 改造新增的菜单与按钮）" \
  system_menu

echo "[3/10] 03-role.sql"
export_data_file "$OUT_DIR/03-role.sql" \
  "角色与授权（system_role + system_role_menu 全量，含 8 个审批岗位角色）" \
  system_role system_role_menu

echo "[4/10] 04-dict.sql"
export_data_file "$OUT_DIR/04-dict.sql" \
  "字典（system_dict_type + system_dict_data 全量）" \
  system_dict_type system_dict_data

echo "[5/10] 05-code-rule.sql"
export_data_file "$OUT_DIR/05-code-rule.sql" \
  "编码规则（infra_code_rule 全量：MDM 18 + WMS 17 + MES 15 + APS 3 + 其他）" \
  infra_code_rule

echo "[6/10] 06-wms-strategy.sql"
export_data_file "$OUT_DIR/06-wms-strategy.sql" \
  "仓储策略样例（wms_strategy）" \
  wms_strategy

echo "[7/10] 07-qc-rule.sql"
export_data_file "$OUT_DIR/07-qc-rule.sql" \
  "质检规则与检验方案样例（mes_qc_rule + mes_qc_plan + mes_qc_item）" \
  mes_qc_rule mes_qc_plan mes_qc_item

echo "[8/10] 08-aps-strategy.sql"
export_data_file "$OUT_DIR/08-aps-strategy.sql" \
  "排程策略样例（aps_strategy）" \
  aps_strategy

echo "[9/10] 09-bpm-process.sql"
export_data_file "$OUT_DIR/09-bpm-process.sql" \
  "五个 MOM 审批流程定义（bpm_category + bpm_form + bpm_process_definition_info + bpm_process_expression；不含 ACT_* 与 FLW_*，引擎自建）" \
  bpm_category bpm_form bpm_process_definition_info bpm_process_expression

# ---- 10：BPMN 模型源文件 ----
# 为什么必须有这一步：bpm_process_definition_info 只是流程的「登记信息」，
# 真正决定流程能不能跑起来的 BPMN 定义存放在 Flowable 的 ACT_RE_PROCDEF /
# ACT_GE_BYTEARRAY 里。按交付规则不许导出 ACT_*，所以「只导 09 号文件」的空库会出现
# 「流程在列表里看得见、点发起却报流程定义不存在」——这正是首次空库实测踩到的坑。
#
# 解法不是去导 ACT_*（违反红线、且会把引擎内部状态一起搬过去），
# 而是把 BPMN 模型 XML 当作「源文件」导出，部署时经 BpmModelController 的
# /import + /deploy 重新部署一次，让引擎自己生成 ACT_* 记录。
echo "[10/10] bpmn/ BPMN 模型源文件"
BPMN_DIR="$OUT_DIR/bpmn"
mkdir -p "$BPMN_DIR"
BPMN_KEYS=("mes-scrap.bpmn" "mes-concession.bpmn" "mes-supplement.bpmn" "mes-op-skip.bpmn" "wms-count-diff.bpmn")
for f in "${BPMN_KEYS[@]}"; do
  # 同一模型可能有多个版本，取 ID_ 最大的一条（= 最新部署的那份）
  if [ "$DUMP_MODE" = "local" ]; then
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" --raw -N \
      -e "SELECT BYTES_ FROM ACT_GE_BYTEARRAY WHERE NAME_='$f' ORDER BY ID_ DESC LIMIT 1" \
      "$MYSQL_DATABASE" > "$BPMN_DIR/$f" 2>/dev/null || true
  else
    docker exec -i -e MYSQL_PWD="$MYSQL_PWD" "$MYSQL_DOCKER_CONTAINER" \
      mysql -u"$MYSQL_USER" --raw -N \
      -e "SELECT BYTES_ FROM ACT_GE_BYTEARRAY WHERE NAME_='$f' ORDER BY ID_ DESC LIMIT 1" \
      "$MYSQL_DATABASE" > "$BPMN_DIR/$f" 2>/dev/null || true
  fi
  if [ -s "$BPMN_DIR/$f" ]; then
    echo "     bpmn/$f  $(wc -c < "$BPMN_DIR/$f") 字节"
  else
    echo "     [WARN] bpmn/$f 取不到内容，请检查源库 ACT_GE_BYTEARRAY" >&2
  fi
done

echo ""
echo "[OK] 导出完成。若本次是交付新版本，请同步更新 sql/deploy/README.md 的变更记录表。"
