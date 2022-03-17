const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    return items.map( it => '\\b' + it.replace(specials, '\\$&') + '\\b').join('|')
}

