import {describe, expect, it} from 'vitest';
import {readFileSync} from 'node:fs';
import {fileURLToPath} from 'node:url';

const sheet = readFileSync(fileURLToPath(new URL('./index.css', import.meta.url)), 'utf8');
const block = (selector: string) => {
    const start = sheet.indexOf(`${selector} {`);
    return sheet.slice(start, sheet.indexOf('}', start));
};

describe('accent tokens', () => {
    it('light root carries the family amber', () => {
        const light = block(':root');
        expect(light).toContain('--primary: 36 76% 55%;');
        expect(light).toContain('--primary-foreground: 43 100% 5%;');
        expect(light).toContain('--accent: 39 89% 92%;');
        expect(light).toContain('--accent-foreground: 36 100% 33%;');
        expect(light).toContain('--ring: 36 100% 33%;');
        expect(light).toContain('--input: 0 0% 70%;');
        expect(light).toContain('--destructive: 346 72% 47%;');
        expect(light).toContain('--sidebar-primary: 36 76% 55%;');
        expect(light).toContain('--sidebar-ring: 36 100% 33%;');
    });

    it('dark carries the family amber', () => {
        const dark = block('.dark');
        expect(dark).toContain('--primary: 36 76% 55%;');
        expect(dark).toContain('--primary-foreground: 43 100% 5%;');
        expect(dark).toContain('--accent: 36 76% 13%;');
        expect(dark).toContain('--accent-foreground: 36 76% 55%;');
        expect(dark).toContain('--ring: 36 76% 55%;');
        expect(dark).toContain('--input: 0 0% 32%;');
        expect(dark).toContain('--destructive: 346 68% 58%;');
        expect(dark).toContain('--sidebar-primary: 36 76% 55%;');
        expect(dark).toContain('--sidebar-ring: 36 76% 55%;');
    });

    it('no blue survives in the sidebar tokens', () => {
        expect(sheet).not.toContain('217.2 91.2% 59.8%');
        expect(sheet).not.toContain('224.3 76.3% 56%');
    });
});
