#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
H2 schema 转换脚本（T6-01 跨模块集成测试基础设施）。

用途：
  1. 通过 mysql 客户端对开发库逐表执行 SHOW CREATE TABLE 导出原始 DDL（原料，缓存到 raw/ 目录）；
  2. 将 MySQL DDL 转换为 H2(MODE=MySQL) 兼容语法，产出
     iboot-server/src/test/resources/integration/schema.sql。

设计约束：
  - 可重复执行（幂等）：raw/ 缓存可复用（--offline 跳过导出、--refresh 强制重导），schema.sql 每次全量重建；
  - 禁止手工编辑转换产物：schema.sql 只能由本脚本生成；
  - 只读数据库：仅执行 SHOW CREATE TABLE。

用法（项目根目录下执行）：
  python script/h2-schema-convert.py                # 导出（复用 raw/ 缓存）+ 转换
  python script/h2-schema-convert.py --refresh      # 强制重新导出全部 DDL
  python script/h2-schema-convert.py --offline      # 仅用 raw/ 缓存转换（不连库）

依赖：mysql 客户端在 PATH 或通过环境变量 MYSQL_CLIENT 指定。
连接参数默认与 iboot-server/src/main/resources/application-local.yaml 保持一致，
可用环境变量覆盖：MYSQL_HOST / MYSQL_PORT / MYSQL_USER / MYSQL_PASSWORD / MYSQL_DB。
"""
import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
RAW_DIR = PROJECT_ROOT / "script" / "h2-schema-convert-raw"
TARGET_FILE = PROJECT_ROOT / "iboot-server" / "src" / "test" / "resources" / "integration" / "schema.sql"
DATA_TARGET_FILE = PROJECT_ROOT / "iboot-server" / "src" / "test" / "resources" / "integration" / "base-data.sql"

# base-data.sql 从开发库导出的表（全量 INSERT）
DATA_EXPORT_TABLES = [
    "system_dict_type",
    "system_dict_data",
    "infra_code_rule",
]
# base-data.sql 里手写最小集的表（用户与角色；集成测试直调 Service 不经 Controller 鉴权，
# 1047 条菜单的 role_menu 全量无意义，给最小授权即可，选择在自查里说明）
DATA_HANDWRITTEN_TABLES = [
    "system_users",
    "system_role",
    "system_user_role",
    "system_role_menu",
]

# ---------------------------------------------------------------------------
# 连接配置（只读）
# ---------------------------------------------------------------------------
MYSQL_HOST = os.environ.get("MYSQL_HOST", "127.0.0.1")
MYSQL_PORT = os.environ.get("MYSQL_PORT", "3306")
MYSQL_USER = os.environ.get("MYSQL_USER", "root")
MYSQL_PASSWORD = os.environ.get("MYSQL_PASSWORD", "123456")
MYSQL_DB = os.environ.get("MYSQL_DB", "ruoyi-vue-pro-20260817")

# 集成测试库不需要的表：
# - ACT_/FLW_：Flowable 引擎自建（CLAUDE.md 红线：ACT_* 表结构由引擎自建），且自带 FK
# - QRTZ_：Quartz 集成测试走 memory job-store，不需要 QRTZ_* 表
# - demo：芋道演示表，业务不用
EXCLUDED_TABLE_PREFIXES = ("ACT_", "FLW_", "QRTZ_", "demo_")

# ---------------------------------------------------------------------------
# 转换规则
# ---------------------------------------------------------------------------
# 列类型映射：MySQL -> H2(MODE=MySQL)。依据开发库类型盘点：
#   bigint/tinyint/int/decimal/varchar/datetime/date/time 保留；
#   json 9 列、text 16 列、mediumblob 1 列、bit(1) 197 列。
COLUMN_TYPE_MAP = {
    "json": "CLOB",
    "mediumtext": "CLOB",
    "longtext": "CLOB",
    "text": "CLOB",
    "tinytext": "CLOB",
    "mediumblob": "BLOB",
    "longblob": "BLOB",
    "tinyblob": "BLOB",
    "blob": "BLOB",
    "enum": "VARCHAR(255)",
    "set": "VARCHAR(255)",
}

# MySQL 字符串字面量（含 \' 与 '' 转义）
STRING_LITERAL_RE = re.compile(r"'(?:[^'\\]|\\.|'')*'")

# 列级 / 索引级 COMMENT '...'（先于其它规则删除）
COMMENT_CLAUSE_RE = re.compile(r"\s+COMMENT\s+" + STRING_LITERAL_RE.pattern, re.IGNORECASE)
# 表级 COMMENT = '...'
TABLE_COMMENT_RE = re.compile(r"\s+COMMENT\s*=\s*" + STRING_LITERAL_RE.pattern, re.IGNORECASE)

# H2 MODE=MySQL 下不支持或无意义的表级子句（顺序：COMMENT 先删，其余在此处理）
TABLE_OPTIONS_PATTERNS = [
    r"\s*ENGINE\s*=\s*\S+",
    r"\s*AUTO_INCREMENT\s*=\s*\d+",
    r"\s*DEFAULT\s+(?:CHARSET|CHARACTER\s+SET)\s*=\s*\S+",
    r"\s*COLLATE\s*=\s*\S+",
    r"\s*ROW_FORMAT\s*=\s*\S+",
]

# 列级 CHARACTER SET / COLLATE 子句
COLUMN_CHARSET_RE = re.compile(
    r"\s+CHARACTER\s+SET\s+\w+|\s+CHARSET\s+\w+|\s+COLLATE\s+\w+", re.IGNORECASE)
# ON UPDATE CURRENT_TIMESTAMP[(n)]：语义由 MyBatis 审计填充器承担，去掉更稳
COLUMN_ON_UPDATE_RE = re.compile(
    r"\s+ON\s+UPDATE\s+CURRENT_TIMESTAMP(?:\(\d*\))?", re.IGNORECASE)
# unsigned / zerofill
COLUMN_UNSIGNED_RE = re.compile(r"\s+(?:unsigned|unsigned\s+zerofill|zerofill)", re.IGNORECASE)
# 整型显示宽度 int(11) / tinyint(4) -> int / tinyint（decimal(20,6) 不在匹配范围）
INT_DISPLAY_WIDTH_RE = re.compile(
    r"\b(tinyint|smallint|mediumint|int|integer|bigint)\(\d+\)", re.IGNORECASE)
# bit(1) -> BOOLEAN；bit(N>1) -> BINARY(ceil(N/8))
BIT_RE = re.compile(r"\bbit\((\d+)\)", re.IGNORECASE)
# bit 默认值字面量 b'0' / b'1'
BIT_LITERAL_RE = re.compile(r"\bb'([01])'", re.IGNORECASE)

# 行首关键字（索引 / 约束行），不按列定义处理
INDEX_LINE_KEYWORDS = ("PRIMARY KEY", "UNIQUE KEY", "UNIQUE INDEX", "UNIQUE", "KEY",
                       "INDEX", "CONSTRAINT", "FULLTEXT", "SPATIAL", "FOREIGN KEY", "CHECK")


def run_mysql(sql: str) -> str:
    client = os.environ.get("MYSQL_CLIENT", "mysql")
    cmd = [
        client,
        f"-h{MYSQL_HOST}", f"-P{MYSQL_PORT}", f"-u{MYSQL_USER}",
        f"-p{MYSQL_PASSWORD}", MYSQL_DB,
        "--batch", "--raw", "--default-character-set=utf8mb4",
    ]
    proc = subprocess.run(cmd, input=sql.encode("utf-8"),
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if proc.returncode != 0:
        raise RuntimeError(f"mysql client failed: {proc.stderr.decode('utf-8', 'replace')}")
    out = proc.stdout.decode("utf-8")
    # mysql --batch 在 Windows 上输出 CRLF，且 SHOW CREATE TABLE 值内换行被原样保留，
    # 实测行尾为 \r\r\n，统一归一为 \n
    return out.replace("\r\r\n", "\n").replace("\r\n", "\n").replace("\r", "\n")


def list_tables() -> list[str]:
    out = run_mysql(
        "SELECT table_name FROM information_schema.TABLES "
        f"WHERE table_schema='{MYSQL_DB}' AND table_type='BASE TABLE' ORDER BY table_name;")
    lines = [ln.strip() for ln in out.splitlines()[1:] if ln.strip()]
    return [ln for ln in lines if not ln.lower().startswith("table_name")]


def export_raw_ddl(tables: list[str], refresh: bool = False) -> dict[str, str]:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    ddl_map: dict[str, str] = {}
    for t in tables:
        raw_file = RAW_DIR / f"{t}.sql"
        if raw_file.exists() and not refresh:
            ddl_map[t] = raw_file.read_text(encoding="utf-8")
            continue
        out = run_mysql(f"SHOW CREATE TABLE `{t}`;")
        # 输出形如：首行 header，之后 `表名\tCREATE TABLE ...`（DDL 含换行）
        parts = out.split("\t", 1)
        if len(parts) != 2 or not parts[1].lstrip().startswith("CREATE TABLE"):
            print(f"[WARN] SHOW CREATE TABLE {t} 解析失败，跳过", file=sys.stderr)
            continue
        ddl = parts[1].strip()
        # 去掉 mysql --batch 转义的表名内反引号输出差异（\` 不存在，防御性处理）
        raw_file.write_text(ddl, encoding="utf-8")
        ddl_map[t] = ddl
    return ddl_map


def convert_column_line(stripped: str) -> str:
    """列定义行：类型转换 + 子句清理。"""
    m = re.match(r"^`?([A-Za-z_][A-Za-z0-9_]*)`?\s+(.+)$", stripped, re.DOTALL)
    if not m:
        return stripped
    name, rest = m.group(1), m.group(2)
    # 类型 token（可能带 (n[,m])）
    tm = re.match(r"^([a-zA-Z]+)(\(\d+(?:\s*,\s*\d+)?\))?", rest)
    if not tm:
        return stripped
    base_type, suffix, after = tm.group(1), tm.group(2) or "", rest[tm.end():]
    full_type = base_type + suffix

    # bit 特判
    bm = BIT_RE.fullmatch(full_type.strip())
    if bm:
        width = int(bm.group(1))
        new_type = "BOOLEAN" if width == 1 else f"BINARY({(width + 7) // 8})"
    else:
        base_lower = base_type.lower()
        new_type = COLUMN_TYPE_MAP.get(base_lower, full_type)

    # 子句清理
    after = COLUMN_UNSIGNED_RE.sub("", after)
    after = COLUMN_CHARSET_RE.sub("", after)
    after = COLUMN_ON_UPDATE_RE.sub("", after)
    # 整型显示宽度
    if new_type == full_type:  # 未映射的类型才做显示宽度归一，decimal 等带精度的不受影响
        after = INT_DISPLAY_WIDTH_RE.sub(lambda mm: mm.group(1), after)
    else:
        # 类型本身已替换（如 json->CLOB），after 里不应再出现整型宽度
        after = INT_DISPLAY_WIDTH_RE.sub(lambda mm: mm.group(1), after)
    # bit 默认值
    if new_type == "BOOLEAN":
        after = BIT_LITERAL_RE.sub(lambda mm: "FALSE" if mm.group(1) == "0" else "TRUE", after)

    return f"{name} {new_type}{after}"


def convert_ddl(ddl: str, table: str = "") -> str:
    # 0) 行尾归一（防御 raw 缓存遗留 CRLF）
    ddl = ddl.replace("\r\r\n", "\n").replace("\r\n", "\n").replace("\r", "\n")

    # 1) 列级/索引级 COMMENT（原文上直接删）
    ddl = COMMENT_CLAUSE_RE.sub("", ddl)
    # 2) 表级 COMMENT = '...'
    ddl = TABLE_COMMENT_RE.sub("", ddl)
    # 3) 表级选项（ENGINE / AUTO_INCREMENT=N / DEFAULT CHARSET / COLLATE / ROW_FORMAT）
    for pat in TABLE_OPTIONS_PATTERNS:
        ddl = re.sub(pat, "", ddl, flags=re.IGNORECASE)

    # 4) 行级处理
    lines = ddl.split("\n")
    out_lines: list[str] = []
    for line in lines:
        lead = line[: len(line) - len(line.lstrip())]
        stripped = line.strip()
        if not stripped:
            out_lines.append(line)
            continue
        upper = stripped.upper()
        if upper.startswith(INDEX_LINE_KEYWORDS):
            # 索引/约束行：去掉 USING BTREE
            stripped = re.sub(r"\s+USING\s+BTREE", "", stripped, flags=re.IGNORECASE)
        else:
            stripped = convert_column_line(stripped)
        out_lines.append(lead + stripped)
    ddl = "\n".join(out_lines)

    # 5) 去反引号（H2 MODE=MySQL 两者都认，统一去引号，与单测 DDL 风格一致）
    ddl = ddl.replace("`", "")

    # 5.1) 索引名加表名前缀：MySQL 的索引名只在表内唯一，H2 是「库内全局唯一」，
    #   多张表都有 idx_create_time / uk_xxx 这类通用名时，第二张表建索引直接报
    #   「Index "xxx" already exists」。去反引号后再做，否则匹配不到被引号包住的名字。
    ddl = "\n".join(prefix_index_name(line, table) for line in ddl.split("\n"))

    # 6) 幂等：CREATE TABLE -> CREATE TABLE IF NOT EXISTS
    ddl = re.sub(r"CREATE TABLE\s+", "CREATE TABLE IF NOT EXISTS ", ddl, count=1)

    # 7) 清理表选项删除后留下的杂散逗号/空白
    ddl = re.sub(r",\s*\)", "\n)", ddl)
    ddl = re.sub(r"\(\s+\n", "(\n", ddl)

    return ddl.strip().rstrip(";") + ";"


def prefix_index_name(line: str, table: str) -> str:
    """给索引 / 唯一约束名加表名前缀，规避 H2 的库级索引名唯一约束。

    只处理索引/约束行（首关键字命中 {@link INDEX_LINE_KEYWORDS}），列行原样返回。
    """
    if not table:
        return line
    # 保留行首缩进与行尾（\r 等），只在中间做替换
    m = re.match(r"^(\s*)(UNIQUE\s+KEY|UNIQUE\s+INDEX|UNIQUE|KEY|INDEX|FULLTEXT|SPATIAL)"
                 r"(\s+)([A-Za-z_][A-Za-z0-9_]*)", line, re.IGNORECASE)
    if not m:
        return line
    indent, keyword, gap, name = m.group(1), m.group(2), m.group(3), m.group(4)
    prefix = f"{table}_"
    if name.startswith(prefix):
        return line
    return f"{indent}{keyword}{gap}{prefix}{name}{line[m.end():]}"


def get_table_columns(table: str) -> list[dict]:
    out = run_mysql(
        "SELECT column_name, data_type FROM information_schema.COLUMNS "
        f"WHERE table_schema='{MYSQL_DB}' AND table_name='{table}' ORDER BY ordinal_position;")
    lines = [ln.strip() for ln in out.splitlines()[1:] if ln.strip()]
    cols = []
    for ln in lines:
        name, dtype = ln.split("\t")
        cols.append({"name": name, "type": dtype})
    return cols


def export_base_data() -> str:
    """从开发库导出字典/编码规则数据并转成 H2 兼容 INSERT。

    取值安全：`mysql --batch` 只把换行转成 `\\n` 字面量，**制表符原样输出**，
    而 `\\t` 正是列分隔符——字段里含 TAB 就会把一行撑成 N+1 段，
    拼接 INSERT 时列数对不上（H2 报 Column count does not match）。
    因此在 SQL 侧就把 TEXT/VARCHAR 列里的 TAB / CR / LF 替换掉，
    再落成单行 INSERT；不靠"行数不够就合并到上一行"的事后补救（那只会把坏行糊成更坏的行）。
    """
    sections: list[str] = []
    for table in DATA_EXPORT_TABLES:
        # bit 列（deleted）在 mysql batch 输出里是原始字节，必须 CAST 成数值才能进 SQL 文本
        columns = get_table_columns(table)
        select_items = []
        for c in columns:
            if c["type"] == "bit":
                select_items.append(f"CAST({c['name']} AS UNSIGNED) AS {c['name']}")
            elif c["type"] in ("text", "mediumtext", "longtext", "tinytext",
                               "varchar", "char", "json"):
                # 制表符是列分隔符、换行是行分隔符，两者都必须在 SQL 侧清掉
                select_items.append(
                    f"REPLACE(REPLACE(REPLACE({c['name']}, CHAR(9), ' '), CHAR(13), ''), CHAR(10), ' ')"
                    f" AS {c['name']}")
            else:
                select_items.append(c["name"])
        out = run_mysql(
            f"SELECT {', '.join(select_items)} FROM `{table}` WHERE deleted=0 ORDER BY id;")
        lines = out.split("\n")
        header = lines[0].split("\t")
        rows: list[list[str]] = []
        for ln in lines[1:]:
            if not ln:
                continue
            cols = ln.split("\t")
            if len(cols) != len(header):
                # 到这里仍然对不上，说明还有未识别的控制字符，宁可报错也不要写出坏 SQL
                raise RuntimeError(
                    f"{table} 第 {len(rows) + 1} 行解析出 {len(cols)} 列，期望 {len(header)} 列：{ln[:200]!r}")
            rows.append(cols)
        col_list = ", ".join(f"`{c}`" for c in header)
        bit_col_idx = [i for i, c in enumerate(header) if any(
            x["name"] == c and x["type"] == "bit" for x in columns)]
        values_sql: list[str] = []
        for cols in rows:
            vals = []
            for i, v in enumerate(cols):
                if i in bit_col_idx:
                    vals.append("TRUE" if v.strip() not in ("0", "") else "FALSE")
                elif v == "NULL":
                    vals.append("NULL")
                elif re.fullmatch(r"-?\d+(\.\d+)?", v):
                    vals.append(v)
                else:
                    # MySQL batch 输出已把换行/制表符转义为 \n \t 字面量；SQL 字符串里需再次转义反斜杠
                    escaped = v.replace("\\", "\\\\").replace("'", "''")
                    vals.append(f"'{escaped}'")
            values_sql.append(f"({', '.join(vals)})")
        block = (f"-- ----------------------------\n-- {table}（{len(rows)} 行，导出自开发库）\n"
                 f"-- ----------------------------\nDELETE FROM {table};\n"
                 f"INSERT INTO {table} ({col_list}) VALUES\n" + ",\n".join(values_sql) + ";\n")
        sections.append(block)
    return "\n".join(sections)


HANDWRITTEN_BASE_DATA = """-- ----------------------------
-- system_users / system_role / system_user_role / system_role_menu（手写最小集）
-- 集成测试直调 Service 层不经 Controller 鉴权；登录链路由 Security mock-enable 承担，
-- 这里只保证表里有可关联的测试用户与角色数据（system_users 密码为 admin 的 BCrypt 串）。
-- ----------------------------
DELETE FROM system_users WHERE id = 4L + 0;
"""

# 手写最小集（占位，稍后由 generate_handwritten_base_data 覆盖）
HANDWRITTEN_BASE_DATA = """
-- ----------------------------
-- system_users（1 个测试用户）
-- ----------------------------
DELETE FROM system_users;
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (104, 'integration-test', '$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9t9SRKOSYCHZCx.aGZChKa', '集成测试用户', 'T6-01 集成测试最小集', 0, '[]', '', '', 0, '', 0, '', NULL, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, FALSE, 0);

-- ----------------------------
-- system_role（1 个测试角色）
-- ----------------------------
DELETE FROM system_role;
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1999, '集成测试角色', 'integration-test', 9999, 1, '', 0, 2, 'T6-01 集成测试最小集', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, FALSE, 0);

-- ----------------------------
-- system_user_role
-- ----------------------------
DELETE FROM system_user_role;
INSERT INTO system_user_role (id, user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1999, 104, 1999, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, FALSE, 0);

-- ----------------------------
-- system_role_menu（最小授权：不做菜单树全量，集成测试直调 Service 不走菜单鉴权）
-- ----------------------------
DELETE FROM system_role_menu;
INSERT INTO system_role_menu (id, role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1999, 1999, 1, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, FALSE, 0);
"""


def export_base_data_section() -> str:
    exported = export_base_data()
    return exported + HANDWRITTEN_BASE_DATA


def scan_key_sequences() -> list[str]:
    """扫描 iboot-server 依赖树内业务模块的 @KeySequence 注解，收集序列名。"""
    seq_names: set[str] = set()
    modules = ["iboot-module-mdm", "iboot-module-wms", "iboot-module-mes",
               "iboot-module-aps", "iboot-module-system", "iboot-module-infra",
               "iboot-module-bpm"]
    pat = re.compile(r'@KeySequence\("([^"]+)"\)')
    for mod in modules:
        for java in (PROJECT_ROOT / mod).rglob("*.java"):
            try:
                text = java.read_text(encoding="utf-8", errors="replace")
            except OSError:
                continue
            seq_names.update(pat.findall(text))
    return sorted(seq_names)


def main() -> None:
    refresh = "--refresh" in sys.argv
    offline = "--offline" in sys.argv
    if offline:
        if not RAW_DIR.exists():
            print("[ERROR] --offline 但 raw 缓存目录不存在，请先在线运行一次", file=sys.stderr)
            sys.exit(1)
        tables = sorted(p.stem for p in RAW_DIR.glob("*.sql"))
        ddl_map = {t: (RAW_DIR / f"{t}.sql").read_text(encoding="utf-8") for t in tables}
        print(f"[offline] 从缓存加载 {len(tables)} 张表")
    else:
        all_tables = list_tables()
        tables = [t for t in all_tables if not t.startswith(EXCLUDED_TABLE_PREFIXES)]
        skipped = [t for t in all_tables if t.startswith(EXCLUDED_TABLE_PREFIXES)]
        print(f"[export] 全库 {len(all_tables)} 张表，纳入 {len(tables)} 张，排除 {len(skipped)} 张")
        print("[export] 排除明细：ACT_/FLW_(Flowable 引擎自建)、QRTZ_(Quartz memory 模式)、demo_(演示表)")
        ddl_map = export_raw_ddl(tables, refresh=refresh)

    converted = [(t, convert_ddl(d, t)) for t, d in sorted(ddl_map.items())]

    header = """-- ============================================================================
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

"""

    TARGET_FILE.parent.mkdir(parents=True, exist_ok=True)
    body = "\n\n".join(f"-- ----------------------------\n-- {t}\n-- ----------------------------\n{ddl}"
                       for t, ddl in converted)
    TARGET_FILE.write_text(header + body + "\n", encoding="utf-8")
    print(f"[done] 写出 {TARGET_FILE}（{len(converted)} 张表）")

    # 序列：H2 下 id-type 自动适配为 INPUT，带 @KeySequence 的 DO 走
    # H2KeyGenerator（select nextval('xxx_seq')），必须预建同名序列。
    seq_names = scan_key_sequences()
    seq_block = ("-- ----------------------------\n"
                 "-- @KeySequence 序列（MyBatis-Plus H2KeyGenerator 依赖）\n"
                 "-- ----------------------------\n"
                 + "\n".join(f"CREATE SEQUENCE IF NOT EXISTS {s} START WITH 1 INCREMENT BY 1;"
                             for s in seq_names) + "\n")
    with open(TARGET_FILE, "a", encoding="utf-8") as f:
        f.write("\n" + seq_block)
    print(f"[done] 追加 {len(seq_names)} 个 @KeySequence 序列")

    # base-data.sql（--no-data 跳过）
    if "--no-data" not in sys.argv:
        data_header = """-- ============================================================================
-- base-data.sql —— 跨模块集成测试基础数据（由脚本生成，禁止手工编辑）
-- 生成方式：python script/h2-schema-convert.py
-- 内容：
--   1. system_dict_type / system_dict_data（字典类型与数据，deleted=0 全量，导出自开发库）
--   2. infra_code_rule（编码规则 54 条：MDM 20 + MES 15 + WMS 14 + APS 3 + PO 2，导出自开发库）
--   3. system_users / system_role / system_user_role / system_role_menu（手写最小集）
--   4. infra_code_sequence 由 CodeRuleService 在生成编码时自动落行，无需预置
-- ============================================================================

"""
        DATA_TARGET_FILE.write_text(data_header + export_base_data_section(), encoding="utf-8")
        print(f"[done] 写出 {DATA_TARGET_FILE}")


if __name__ == "__main__":
    main()
