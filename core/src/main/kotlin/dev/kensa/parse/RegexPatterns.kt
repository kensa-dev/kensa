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
    val fixturesFactoryPattern: Regex = """^fixtures[\[({](?:(?<receiver>\w+)\.)?(?<function>\w+)\((?<args>.*)\)[])}](?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val callWithArgumentsAndPathPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\(.+?\)(?:!!)?\??\.(?<path>$CHAIN)$""".toRegex()
}
