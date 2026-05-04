export function injectBaseHref(content: string, absoluteBase: string): string {
    if (/<base\s/i.test(content)) return content;
    const baseTag = `<base href="${absoluteBase}">`;
    if (/<head[^>]*>/i.test(content)) {
        return content.replace(/<head[^>]*>/i, (m) => `${m}${baseTag}`);
    }
    if (/<html[^>]*>/i.test(content)) {
        return content.replace(/<html[^>]*>/i, (m) => `${m}<head>${baseTag}</head>`);
    }
    return `${baseTag}${content}`;
}
