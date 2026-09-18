// Drift guard: three quoting rules hold one script; nothing but this keeps them the same.
import {describe, expect, it} from 'vitest';
import {readFileSync} from 'node:fs';
import {fileURLToPath} from 'node:url';

const extractScript = (relativePath: string): string => {
    const text = readFileSync(fileURLToPath(new URL(relativePath, import.meta.url)), 'utf8');
    const match = /^[ \t]*<script>[\s\S]*?<\/script>/m.exec(text);
    if (!match) throw new Error(`no <script> block found in ${relativePath}`);
    const lines = match[0].split('\n');
    const indents = lines
        .filter(line => line.trim().length > 0)
        .map(line => line.match(/^[ \t]*/)![0].length);
    const commonIndent = Math.min(...indents);
    return lines.map(line => line.slice(commonIndent)).join('\n');
};

const devScript = extractScript('../index.html');

describe('theme stamp', () => {
    it('the report page carries the same theme stamp as the dev page', () => {
        expect(extractScript('../../core/src/main/kotlin/dev/kensa/output/ResultWriter.kt')).toBe(devScript);
    });

    it('the CLI shell carries the same theme stamp as the dev page', () => {
        expect(extractScript('../../cli/internal/shell/embed/index.html')).toBe(devScript);
    });
});
