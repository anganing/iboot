/**
 * 把富文本 HTML 转成纯文本
 *
 * <p>列表单元格与消息提醒里不能出现 `<p>` 这类标签，但也不能直接正则剥标签：
 * 那样会把 `&nbsp;` 之类实体留在结果里。交给浏览器解析最省事。
 *
 * @param html 富文本内容
 * @param maxLength 超长时截断并加省略号，不传则不截断
 */
export function htmlToText(html?: string, maxLength?: number): string {
  if (!html) {
    return '';
  }
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const text = (doc.body.textContent ?? '').replaceAll(/\s+/g, ' ').trim();
  if (!maxLength || text.length <= maxLength) {
    return text;
  }
  return `${text.slice(0, maxLength)}…`;
}
