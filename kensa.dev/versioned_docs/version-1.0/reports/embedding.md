---
title: Embedding a Test in Another Page
sidebar_label: Embedding
sidebar_position: 2
description: A chromeless view of one test class or method at #/embed/<id>, sized by the host page through kensa-embed.js, for Confluence pages, design docs, service catalogs and anywhere else a live report belongs.
---

# Embedding

A Kensa report can render one test class, or one method of it, with no sidebar, header or search: just the test cards and a slim footer linking back to the full report. Put that view in an `<iframe>` and the page that owns a decision (an acceptance criteria page, a design doc, a story) shows the test that proves it, live from the latest run.

## The embed URL

```
index.html#/embed/<testId>?method=<name>&invocation=<n>&theme=light|dark|auto&notes=1
```

The quickest way to get one is the **Copy embed link** control that appears beside **Copy link** when you hover a test's header in the report, or the class name in the report header. The first copies the URL for that method, the second for the whole class, ready to paste.

| Parameter | Effect |
|-----------|--------|
| `method` | Show only this method, expanded. Without it the whole class renders, every method collapsed, or the single method expanded when there is only one. |
| `invocation` | For a parameterised method, expand this invocation (zero based). |
| `theme` | `light`, `dark`, or `auto` (the default), which follows the reader's OS setting. The embed never writes the choice back to the reader's stored preference. |
| `notes` | `1` shows the class notes card above the tests. Off by default. |

An unknown test id or method renders a short message with a link to the full report rather than an empty frame.

## Sizing the frame

Hosts fall into two groups.

### Hosts that can run a script

The embed posts its own height to the parent page each time its content changes (a method expanded, a tab opened). Include `kensa-embed.js`, which ships beside `kensa.js` in every report, and mark each frame with `data-kensa-embed`:

```html
<iframe data-kensa-embed
        src="https://reports.example.com/index.html#/embed/main::com.example.OrderServiceTest?method=order%20is%20accepted&theme=auto"
        style="width: 100%; border: 0;"></iframe>

<script src="https://reports.example.com/kensa-embed.js"></script>
```

Any number of frames can share a page. Each one is matched to the window that sent the message, so no ids are needed, and a frame added after the page loads is picked up too. The script has no dependencies and can be included once, anywhere on the page.

The message is `{type: 'kensa:height', height: <pixels>}` posted to the parent with a `*` target origin, so a host on any domain can read it. If you would rather size the frame yourself, listen for that message and ignore the script.

### Hosts that cannot

Confluence's iframe macro, and most wiki and CMS embeds, do not let you add a script. Give the frame a fixed height instead:

```html
<iframe src="https://reports.example.com/index.html#/embed/main::com.example.OrderServiceTest?method=order%20is%20accepted"
        width="100%" height="600" style="border: 0;"></iframe>
```

The embed scrolls inside the frame when the content is taller, and nothing it does on load can scroll the host page.

## A stable address for the links

Links copied from a report point at the page they were copied from. For a report opened from a local build directory that is a `file://` URL, which is no use to anyone else. Publish the report at an address that always resolves to the latest run, then tell Kensa that address, and every copied link (deep links and embed links alike) uses it as its base:

- a CI artifact URL such as TeamCity's `.lastSuccessful`, or
- any published site, for instance a report built with [site mode](../build-plugins/site-mode.md).

Set it in code with `withLinkBaseUrl` (see [Configuration](../api/configuration.md#link-base-url)), or from the CI job with the `KENSA_LINK_BASE_URL` environment variable, which wins when both are set so no test code needs to know the build server's URL. A base ending in `/` is treated as a directory and `index.html` is appended.

Without a base, links keep using the page's own location, as before.

## Pasting a link

Paste a copied embed link into Slack, Microsoft Teams, a GitHub issue or pull request, or a Jira Cloud story and the host shows a card: the test's name, its state and class, and its sentences as text. Jira cannot iframe a page without an app installed, but it unfurls links natively, so a story with the link on it shows whether the behaviour is proven without a click.

The card comes from a static page Kensa writes beside the report. The report itself is one page whose hash route names the test, and a host fetching a link never sees the hash, so every class and method gets a page of its own under `embed/` in the report bundle, carrying [Open Graph](https://ogp.me) tags and a refresh on to the live embed. A person who opens the link lands on the embedded test as before; a host that fetches it reads the card. Beside the site name the card shows the Kensa mark, served as `favicon.png` from the report bundle.

The pages are written only when the report has a [stable address](#a-stable-address-for-the-links). Without a `linkBaseUrl` there is nothing a host could fetch, so **Copy embed link** copies the hash URL as before; with one it copies the page URL, `<base>/embed/<class>/<method>.html`, or `index.html` for the whole class. In a [site](../build-plugins/site-mode.md) each source bundle keeps its own `embed/` directory and the copied link goes through it.

A parameterised method's card shows the sentences of its first invocation. The card is text only in 1.0; a picture of the sentences comes in 1.0.1 at the same address.

## Reading a long test

In the full report, once a test is expanded and its card header has scrolled off the top, the report header names that test with its state icon in place of the class name. Click the name to scroll back to the card. The header's copy-link copies the deep link for that method while it is showing. Collapsed cards never take the header, so scrolling past tests you have not opened leaves the class name in place. The embed view has no header, so this does not apply inside a frame.

## What the footer shows

Below the test cards the embed has one line: the Kensa mark, the test class, its state, and **Open full report**, which opens the same test in the full report in a new tab. It is hidden when the embed is printed, so a printed page carries the test alone.
