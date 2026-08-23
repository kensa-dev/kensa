import { describe, it, expect } from 'vitest';
import { overviewPathFor } from './overviewPath';

describe('overviewPathFor', () => {
    it('builds a bare overview path when search is empty', () => {
        expect(overviewPathFor('', 'a')).toBe('/overview?source=a');
    });

    it('preserves existing query params', () => {
        expect(overviewPathFor('?q=x', 'a')).toBe('/overview?q=x&source=a');
    });

    it('drops the test-level method and invocation params', () => {
        expect(overviewPathFor('?q=x&method=m&invocation=2', 'a')).toBe('/overview?q=x&source=a');
    });

    it('replaces an existing source param', () => {
        expect(overviewPathFor('?source=b', 'a')).toBe('/overview?source=a');
    });
});
