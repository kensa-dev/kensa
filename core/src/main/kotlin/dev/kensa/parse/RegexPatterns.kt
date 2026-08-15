package dev.kensa.parse

internal object RegexPatterns {
    private const val TYPE_ARGS = """(?:<[^(]+>)?"""
    private const val SEGMENT = """\w+$TYPE_ARGS(?:\(\))?(?:!!)?"""
    private const val CHAIN = """$SEGMENT(?:\??\.$SEGMENT)*"""

    val chainedCallPattern: Regex = """^(\w+)(\(\))?(?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val fixturesPattern: Regex = """^fixtures$TYPE_ARGS[\[({](?:\w+\.)*(\w+)[\])}](?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val outputsByNamePattern: Regex = """^outputs$TYPE_ARGS[\[(](?:\w+\.)*(\w+)[])](?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val outputsByKeyPattern: Regex = """^outputs$TYPE_ARGS\("([^"]+)"\)(?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val singleCallWithArgumentsPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\((?<args>.*)\)$""".toRegex()

    // The greedy args group also matches `f(x).g(y)` as one call to f; reject
    // matches whose args are not paren-balanced so those fall through (#181).
    fun matchSingleCallWithArguments(text: String): MatchResult? =
        singleCallWithArgumentsPattern.matchEntire(text)?.takeIf { it.groups["args"]!!.value.hasBalancedParens() }

    private fun String.hasBalancedParens(): Boolean {
        var depth = 0
        for (c in this) {
            when (c) {
                '(' -> depth++
                ')' -> if (--depth < 0) return false
            }
        }
        return depth == 0
    }
    val fixturesFactoryPattern: Regex = """^fixtures[\[({](?:(?<receiver>\w+)\.)?(?<function>\w+)\((?<args>.*)\)[])}](?:!!)?(?:\??\.(?<path>$CHAIN))?$""".toRegex()
    val callWithArgumentsAndPathPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\(.+?\)(?:!!)?\??\.(?<path>$CHAIN)$""".toRegex()
}
