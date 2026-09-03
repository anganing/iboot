/** 匹配 CJK 统一汉字及扩展区、日文假名、韩文音节 */
const CJK_REGEX =
  /[\u3040-\u30FF\u3400-\u4DBF\u4E00-\u9FFF\uAC00-\uD7AF\uF900-\uFAFF]/u;

/**
 * 取首字符，拉丁字母统一转为大写
 */
function firstChar(text: string): string {
  const chars = [...text];
  return (chars[0] ?? '').toUpperCase();
}

/**
 * 生成文字头像的展示字符，用于用户没有头像图片时的回退展示。
 *
 * <p>规则与头像尺寸无关，小尺寸靠字号适配，不改变字符数：
 * - 空值返回 `?`
 * - 含空白：取前两段的首字符，如 `jack ma` → `JM`、`张 三` → `张三`
 * - 无空白且含中日韩文字：取末两个字符，如 `张伟` → `张伟`、`王小明` → `小明`
 * - 其余取首字符大写，如 `admin` → `A`
 *
 * @param name 用户昵称或账号
 */
function getAvatarInitials(name?: null | string): string {
  const trimmed = (name ?? '').trim();
  if (!trimmed) {
    return '?';
  }

  const segments = trimmed.split(/\s+/).filter(Boolean);
  if (segments.length > 1) {
    return `${firstChar(segments[0] ?? '')}${firstChar(segments[1] ?? '')}`;
  }

  // 按码点切分，避免把 emoji 等代理对切成乱码
  const chars = [...trimmed];
  if (CJK_REGEX.test(trimmed)) {
    return chars.slice(-2).join('');
  }
  return firstChar(trimmed);
}

export { getAvatarInitials };
