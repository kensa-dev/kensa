import {describe, expect, it} from 'vitest';
import {handleMessage, type EmbedFrame} from './embedHost';
import {heightMessage} from '@/util/embedHeight';

const frame = (): EmbedFrame => ({contentWindow: {}, style: {height: ''}});

describe('handleMessage', () => {
    it('resizes only the frame whose window sent the message', () => {
        const f1 = frame();
        const f2 = frame();
        handleMessage([f1, f2], {source: f2.contentWindow, data: heightMessage(500)});
        expect(f2.style.height).toBe('500px');
        expect(f1.style.height).toBe('');
    });

    it('ignores a message from an unknown window', () => {
        const f1 = frame();
        handleMessage([f1], {source: {}, data: heightMessage(500)});
        expect(f1.style.height).toBe('');
    });

    it('ignores data that is not a height message', () => {
        const f1 = frame();
        handleMessage([f1], {source: f1.contentWindow, data: {type: 'other', height: 500}});
        handleMessage([f1], {source: f1.contentWindow, data: 'hello'});
        expect(f1.style.height).toBe('');
    });

    it('ignores a frame without a window', () => {
        const f1: EmbedFrame = {contentWindow: null, style: {height: ''}};
        handleMessage([f1], {source: null, data: heightMessage(500)});
        expect(f1.style.height).toBe('');
    });
});
