export function setStateFilter(query: string, state: string): string {
    const token = `state:${state}`
    const parts = query.split(/\s+/).filter(Boolean)
    const alreadyActive = parts.includes(token)
    const withoutAnyState = parts.filter(p => !p.startsWith("state:"))
    const next = alreadyActive ? withoutAnyState : [...withoutAnyState, token]
    return next.join(" ")
}
