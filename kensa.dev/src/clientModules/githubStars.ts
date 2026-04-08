const REPO = 'kensa-dev/kensa';
const GITHUB_HREF = `https://github.com/${REPO}`;

function updateStarLink(stars: number): void {
    const link = document.querySelector<HTMLAnchorElement>(`a.navbar__item[href="${GITHUB_HREF}"]`);
    if (link) link.textContent = `⭐ Star · ${stars.toLocaleString()}`;
}

export function onRouteDidUpdate(): void {
    const cached = sessionStorage.getItem('gh-stars');
    if (cached) {
        updateStarLink(Number(cached));
        return;
    }
    fetch(`https://api.github.com/repos/${REPO}`)
        .then(r => r.json())
        .then(d => {
            if (typeof d.stargazers_count === 'number') {
                sessionStorage.setItem('gh-stars', String(d.stargazers_count));
                updateStarLink(d.stargazers_count);
            }
        })
        .catch(() => {});
}
