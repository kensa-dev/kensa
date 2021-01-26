const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    items.map( it => it.replace(specials, '\\$&')).join('|')
}

