import {describe, it, expect} from 'vitest';
import {issueBadgeMenu, issueTrackerHref, replayIssueHref, withReplayUrl} from './replayLink';
import {DEFAULT_CONFIG, KensaConfig} from '@/contexts/ConfigContext';
import {Manifest} from '@/types/Manifest';

const manifestWith = (replayUrl?: string): Manifest => ({
    schemaVersion: 1,
    sources: [],
    ...(replayUrl === undefined ? {} : {replayUrl})
});

describe('replayIssueHref', () => {
    it('appends the replay path and issue tag', () => {
        expect(replayIssueHref('https://replay.example.com', 'TEAM-1'))
            .toBe('https://replay.example.com/replay/?tags=issue:TEAM-1');
    });

    it('trims a trailing slash from the base url', () => {
        expect(replayIssueHref('https://replay.example.com/', 'TEAM-1'))
            .toBe('https://replay.example.com/replay/?tags=issue:TEAM-1');
    });

    it('does not double the replay segment when the base already ends with /replay', () => {
        expect(replayIssueHref('https://replay.example.com/replay', 'TEAM-1'))
            .toBe('https://replay.example.com/replay/?tags=issue:TEAM-1');
    });

    it('does not double the replay segment when the base already ends with /replay/', () => {
        expect(replayIssueHref('https://replay.example.com/replay/', 'TEAM-1'))
            .toBe('https://replay.example.com/replay/?tags=issue:TEAM-1');
    });

    it('keeps other path segments intact', () => {
        expect(replayIssueHref('https://example.com/hub/', 'TEAM-1'))
            .toBe('https://example.com/hub/replay/?tags=issue:TEAM-1');
    });

    it('encodes issue ids containing url-unsafe characters', () => {
        expect(replayIssueHref('https://replay.example.com', 'a b&c/d'))
            .toBe('https://replay.example.com/replay/?tags=issue:a%20b%26c%2Fd');
    });

    it('is not confused by a path segment merely containing replay', () => {
        expect(replayIssueHref('https://example.com/replays', 'TEAM-1'))
            .toBe('https://example.com/replays/replay/?tags=issue:TEAM-1');
    });
});

describe('withReplayUrl', () => {
    const config: KensaConfig = {...DEFAULT_CONFIG, titleText: 'Suite'};

    it('adds the manifest replay url to the config', () => {
        expect(withReplayUrl(config, manifestWith('https://replay.example.com')))
            .toEqual({...config, replayUrl: 'https://replay.example.com'});
    });

    it('returns an equal config when the manifest has no replay url', () => {
        expect(withReplayUrl(config, manifestWith())).toEqual(config);
    });

    it('ignores a blank manifest replay url', () => {
        expect(withReplayUrl(config, manifestWith('   '))).toEqual(config);
    });

    it('overrides any replay url already on the config', () => {
        const existing: KensaConfig = {...config, replayUrl: 'https://old.example.com'};
        expect(withReplayUrl(existing, manifestWith('https://new.example.com')).replayUrl)
            .toBe('https://new.example.com');
    });

    it('clears a config replay url when the manifest has none', () => {
        const existing: KensaConfig = {...config, replayUrl: 'https://old.example.com'};
        expect(withReplayUrl(existing, manifestWith()).replayUrl).toBeUndefined();
    });

    it('omits the replay url key entirely when the manifest has none', () => {
        const existing: KensaConfig = {...config, replayUrl: 'https://old.example.com'};
        expect('replayUrl' in withReplayUrl(existing, manifestWith())).toBe(false);
    });

    it('does not mutate the input config', () => {
        const input: KensaConfig = {...config};
        withReplayUrl(input, manifestWith('https://replay.example.com'));
        expect(input.replayUrl).toBeUndefined();
    });
});

describe('issueTrackerHref', () => {
    it('joins the tracker url and the issue id', () => {
        expect(issueTrackerHref('https://jira.example.com/browse', 'TEAM-1'))
            .toBe('https://jira.example.com/browse/TEAM-1');
    });

    it('does not double the separator when the tracker url ends with a slash', () => {
        expect(issueTrackerHref('https://jira.example.com/browse/', 'TEAM-1'))
            .toBe('https://jira.example.com/browse/TEAM-1');
    });
});

describe('issueBadgeMenu', () => {
    it('is empty when neither url is configured', () => {
        expect(issueBadgeMenu('', undefined, 'TEAM-1')).toEqual([]);
    });

    it('offers only the issue entry when just the tracker url is configured', () => {
        expect(issueBadgeMenu('https://jira.example.com/browse', undefined, 'TEAM-1')).toEqual([
            {key: 'issue', label: 'Open issue', href: 'https://jira.example.com/browse/TEAM-1'}
        ]);
    });

    it('offers only the replay entry when just the replay url is configured', () => {
        expect(issueBadgeMenu('', 'https://replay.example.com', 'TEAM-1')).toEqual([
            {key: 'replay', label: 'Open in Replay', href: 'https://replay.example.com/replay/?tags=issue:TEAM-1'}
        ]);
    });

    it('offers the issue entry before the replay entry when both are configured', () => {
        expect(issueBadgeMenu('https://jira.example.com/browse/', 'https://replay.example.com/replay/', 'TEAM-1')).toEqual([
            {key: 'issue', label: 'Open issue', href: 'https://jira.example.com/browse/TEAM-1'},
            {key: 'replay', label: 'Open in Replay', href: 'https://replay.example.com/replay/?tags=issue:TEAM-1'}
        ]);
    });

    it('ignores blank urls', () => {
        expect(issueBadgeMenu('   ', '   ', 'TEAM-1')).toEqual([]);
    });
});
