# T-GOV 工程治理：依赖版本、行尾规范、lint 基线

- 提出日期：2026-09-16
- 提出背景：M0/M1 执行期间由 subagent 暴露，经主线程独立核实
- 性质：**独立任务，不属于「移动端开发安排」本版范围**
- 为什么独立：三项修复都会产生跨全仓的大面积 diff，与移动端功能改动混在一起将无法分辨 diff 来源，污染功能评审

---

## 问题一：`@wot-ui/ui` 用 `latest` 锁版本，构建不可复现（**优先级最高**）

### 现象

`pnpm type-check` 报错，且**不在本仓库代码里**：

```
node_modules/@wot-ui/ui/components/wd-button/wd-button.vue(21,6): error TS2322:
  Type 'ButtonOpenType' is not assignable to type '_ButtonOpenType'.
  Type '"getRealtimePhoneNumber"' is not assignable to type '_ButtonOpenType'.
```

### 根因（已逐环验证）

1. `iboot-ui/iboot-mobile/package.json:141` → **`"@wot-ui/ui": "latest"`**
2. 实测装到 **2.3.2**（`node_modules/@wot-ui/` 为 pnpm 软链，指向 `@wot-ui+ui@2.3.2_vue@3.4.21_typescript@5.8.3_`）
3. 该版本 wd-button 声明 `open-type` 支持 `getRealtimePhoneNumber`（微信较新 API）
4. 而本项目 `@dcloudio/uni-*` 锁定 `3.0.0-4070620250821001`，`@dcloudio/types` 为 `^3.4.8`——类型定义较旧，不认识该值
5. 两边对不上 → `vue-tsc` 报 TS2322

### 为什么必须修

**`"latest"` 意味着每次 `pnpm install` 都可能拉到不同版本，构建结果不可复现。** 这不是理论风险，实测已经发生：M1 首次交付时 `type-check` 为 exit=0，返工复跑变成 exit=2，期间 M1 只改了一个图标名和一处类型注释，**逻辑上不可能影响 `node_modules` 内三方包的类型解析**。差异来源就是 wot-ui 落到了不同版本。

这次暴露的是类型错误（还好，看得见）；换成运行时行为差异（组件 props 语义变更、样式类名调整），就是**难定位的生产事故**。且该项目 UI 全部 `wd-*` 依赖它，等于全前端建在流沙上。

### 修法

把 `"@wot-ui/ui": "latest"` 改为**确定版本号**。选择策略二选一：

- **保守（推荐）**：锁到当前实测可用的 2.3.2，即 `"@wot-ui/ui": "2.3.2"`。先让构建稳定，不引入新变量。
- **对齐**：若希望消除 `getRealtimePhoneNumber` 类型冲突，需同时升 `@dcloudio/types`（或 uni-app 全家桶）到认识该 API 的版本。**但这会牵动整个 uni-app 工具链，风险远大于收益**，无实际业务需求（本项目不用 `getRealtimePhoneNumber`）时不要做。

**建议走保守方案。** 类型冲突即使存在也不影响运行——我们不用那个 open-type 值，`vue-tsc` 报的是三方包内部的不自洽。

### 验收

```bash
cd D:/iboot-pro/iboot/iboot-ui/iboot-mobile
rm -rf node_modules && pnpm install   # 全量重装
pnpm type-check                        # 期望：零错误
```
连续跑两次 `rm -rf node_modules && pnpm install && pnpm type-check`，结果必须一致（验证可复现性）。

---

## 问题二：行尾规范自相矛盾（CRLF vs LF）

### 现象

`pnpm lint` 全仓失败：

```
✖ 1510 problems (1327 errors, 183 warnings)
  1239 errors and 183 warnings potentially fixable with the `--fix` option.
```

其中 **1239 处**是 `format/prettier` 的 `Insert ␍`（CRLF 行尾）。

### 根因（已逐环验证）

1. `iboot-ui/iboot-mobile/.editorconfig:7` → **`end_of_line = lf`**（子项目明确要求 LF）
2. 仓库根 `D:\iboot-pro\iboot\.gitattributes` 全文只有两行：
   ```
   # Auto detect text files and perform LF normalization
   * text=auto
   ```
3. 配合 `core.autocrlf=true`（本机 git 配置实测值）
4. 实际效果实测：
   - **仓库里存的是 LF**（`git show HEAD:.../debounce.ts | file -` → 无 CRLF 标记）
   - **工作区检出的是 CRLF**（`file src/utils/debounce.ts` → `with CRLF line terminators`）
5. prettier 未显式配 `endOfLine`，默认 `lf` → 与工作区 CRLF 冲突 → 报 `Insert ␍`

**结论：这是配置自相矛盾，不是环境脏。** 子项目的 `.editorconfig` 要 LF，根目录的 git 配置把工作区变成 CRLF。**每个 Windows 检出者都会撞上同一堵墙**，不是个例。

### 修法（三选一，需决策）

| 方案 | 做法 | 代价 |
| --- | --- | --- |
| **A（推荐）** | `core.autocrlf` 改为 `false` + `core.eol=lf`，全仓重新检出 | 需 stash 或 commit 当前改动；一次性解决，工作区回到 LF，「三条门禁」验收标准可照原样执行 |
| B | 根 `.gitattributes` 增 `* text=auto eol=lf` | 需全仓重新检出；比 A 更显式（进版本库，团队一致） |
| C | prettier 显式配 `endOfLine: 'crlf'` | 最省事但**方向错**——与子项目 `.editorconfig` 的 `lf` 声明直接冲突，等于用配置掩盖问题 |

**建议 A 或 B。C 不接受**（它是打补丁，且与 `.editorconfig` 打架）。

### 验收

```bash
cd D:/iboot-pro/iboot/iboot-ui/iboot-mobile
file src/utils/debounce.ts        # 期望：无 CRLF 标记
pnpm lint                          # 期望：CRLF 类错误清零（剩余错误另计）
```

---

## 问题三：lint 基线与「三条门禁」验收标准的落差

### 现象

22 个任务书模板都写「`pnpm type-check && pnpm lint && pnpm test:run` 三条全过才算交付」，但 `pnpm lint` 当前**不可能通过**（1327 errors，含问题二的 CRLF + 存量问题）。

### 需剔除的存量问题（问题二修复后仍存的部分）

- `.agents/skills/uniapp-project/examples/**` —— 模板自带示例文件
- `src/store/index.ts` —— `perfectionist/sort-exports`
- `src/tabbar/TabbarItem.test.ts:18` —— `test/prefer-lowercase-title`
- 其余待问题二修复后重新统计

### 修法

问题二修复后：

1. 重跑 `pnpm lint` 统计**剩余**错误
2. 逐类判断：真问题就修；模板示例文件可加入 eslint `ignores`
3. **把基线刷绿**，之后新增错误即为真实回归，门禁才有意义

### 验收

```bash
pnpm lint   # 期望：exit 0
```

之后新任务的门禁恢复为「三条全过」，本版的「改动文件 `npx eslint` 干净」临时标准作废。

---

## 临时措施（三项修复完成前，本版移动端开发沿用）

1. **验收标准降级**：`type-check` + `test:run` 全过 + **改动文件 `npx eslint <文件>` 干净** + 全量 lint 结果如实上报
2. **`type-check` 判读规则**：只认**本仓库代码**的报错；`node_modules/@wot-ui/**` 内的 TS2322 属问题一，**不计入任务缺陷**
3. 每份任务书的「遗留」列须如实记录当前 lint / type-check 的全量结果与「被谁挡住」

---

## 自查状态

| 项 | 内容 |
| --- | --- |
| 自查状态 | 未开始 |
| 说明 | 本任务为**治理任务**，三个问题均由 M0/M1 执行期间暴露、经主线程独立核实，尚未实施 |
| 审核状态 | 待审查 |
| 审核意见 | 待老大决策修法（问题二的 A/B/C 三选一）；问题一建议直接走保守方案 |
