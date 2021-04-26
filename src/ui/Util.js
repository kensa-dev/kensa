const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    return items.map( it => it.replace(specials, '\\$&')).join('|')
}

