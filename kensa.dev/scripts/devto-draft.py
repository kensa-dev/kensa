#!/usr/bin/env python3
"""Create a dev.to draft from a kensa.dev blog post.

    scripts/devto-draft.py blog/2026-09-11-bdd-without-gherkin.mdx

Reads the API key from macOS Keychain (service DEVTO_API_KEY), never from a file or
argument. The post is created unpublished with canonical_url pointing at kensa.dev, so
publishing is a manual step on dev.to and Google credits the original.

Conversions: Docusaurus front matter -> dev.to fields; `{/* truncate */}` and component
imports dropped; <BlogImage src alt /> -> Markdown image with an absolute kensa.dev URL;
root-relative links made absolute. Everything else is passed through as written.

Python 3 standard library only.
"""
import json
import re
import subprocess
import sys
import urllib.error
import urllib.request

SITE = "https://kensa.dev"
API = "https://dev.to/api/articles"
MAX_TAGS = 4
# dev.to tags are single lowercase words without punctuation; map ours, drop the rest.
TAG_MAP = {"kotlin": "kotlin", "java": "java", "testing": "testing", "bdd": "bdd"}


def keychain(service: str) -> str:
    out = subprocess.run(
        ["security", "find-generic-password", "-s", service, "-w"],
        capture_output=True, text=True, check=True,
    )
    return out.stdout.strip()


def parse(path: str) -> tuple[dict, str]:
    text = open(path, encoding="utf-8").read()
    m = re.match(r"^---\n(.*?)\n---\n(.*)$", text, re.S)
    if not m:
        sys.exit(f"{path}: no front matter")
    front, body = m.group(1), m.group(2)
    meta: dict = {}
    for line in front.splitlines():
        km = re.match(r"^(\w+):\s*(.*)$", line)
        if km:
            meta[km.group(1)] = km.group(2).strip().strip('"')
    tags = re.findall(r"[\w-]+", meta.get("tags", ""))
    meta["tags"] = [TAG_MAP[t] for t in tags if t in TAG_MAP][:MAX_TAGS]
    return meta, body


def convert(body: str) -> str:
    body = re.sub(r"^import .*?;\n", "", body, flags=re.M)
    body = body.replace("{/* truncate */}\n", "")
    body = re.sub(
        r'<BlogImage\s+src="([^"]+)"\s+alt="([^"]*)"\s*/>',
        lambda m: f"![{m.group(2)}]({SITE}{m.group(1)})",
        body,
    )
    body = re.sub(r"\]\((/[^)\s]+)\)", lambda m: f"]({SITE}{m.group(1)})", body)
    return body.strip() + "\n"


def main() -> None:
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    meta, body = parse(sys.argv[1])
    article = {
        "title": meta["title"],
        "body_markdown": convert(body),
        "published": False,
        "tags": meta["tags"],
        "canonical_url": f"{SITE}/blog/{meta['slug']}",
        "description": meta.get("description", ""),
    }
    req = urllib.request.Request(
        API,
        data=json.dumps({"article": article}).encode(),
        headers={
            "api-key": keychain("DEVTO_API_KEY"),
            "Content-Type": "application/json",
            "Accept": "application/vnd.forem.api-v1+json",
            # dev.to sits behind Cloudflare, which answers the default urllib agent with 403.
            "User-Agent": "kensa-devto-draft/1.0 (+https://kensa.dev)",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            created = json.load(r)
    except urllib.error.HTTPError as e:
        sys.exit(f"dev.to {e.code}: {e.read().decode()[:400]}")
    print(f"draft: {created.get('url')}  (id {created.get('id')}, canonical {article['canonical_url']})")


if __name__ == "__main__":
    main()
