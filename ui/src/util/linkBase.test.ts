import {describe, expect, it} from 'vitest';
import {reportBase} from './linkBase';

const location = {origin: 'http://localhost:5173', pathname: '/index.html', search: '?theme=dark'};

describe('reportBase', () => {
    it('uses linkBaseUrl when configured', () => {
        expect(reportBase({linkBaseUrl: 'https://ci.example.com/build/.lastSuccessful/kensa-output/index.html'}, location))
            .toBe('https://ci.example.com/build/.lastSuccessful/kensa-output/index.html');
    });

    it('points a directory base at its index.html', () => {
        expect(reportBase({linkBaseUrl: 'https://reports.example.com/latest/'}, location))
            .toBe('https://reports.example.com/latest/index.html');
    });

    it('falls back to the current location without one', () => {
        expect(reportBase({linkBaseUrl: null}, location)).toBe('http://localhost:5173/index.html?theme=dark');
        expect(reportBase({}, location)).toBe('http://localhost:5173/index.html?theme=dark');
    });

    it('treats a blank linkBaseUrl as unset', () => {
        expect(reportBase({linkBaseUrl: '  '}, location)).toBe('http://localhost:5173/index.html?theme=dark');
    });
});
