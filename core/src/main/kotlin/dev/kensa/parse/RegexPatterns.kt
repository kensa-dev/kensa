package dev.kensa.parse

internal object RegexPatterns {
    private const val SEGMENT = """\w+(?:\(\))?(?:!!)?"""
    private const val CHAIN = """$SEGMENT(?:\??\.$SEGMENT)*"""

    val chainedCallPattern: Regex = """^(\w+)(\(\))?(?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val fixturesPattern: Regex = """^fixtures[\[({](?:\w+\.)*(\w+)[\])}](?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val outputsByNamePattern: Regex = """^outputs[\[(](?:\w+\.)*(\w+)[])](?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val outputsByKeyPattern: Regex = """^outputs\("([a-zA-Z0-9_]+)"\)(?:!!)?(?:\??\.($CHAIN))?$""".toRegex()
    val singleCallWithArgumentsPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\((?<args>.*)\)$""".toRegex()
    val fixturesFactoryPattern: Regex = """^fixtures[\[({](?:(?<receiver>\w+)\.)?(?<function>\w+)\((?<args>.*)\)[\])}](?:!!)?(?:\??\.(?:$CHAIN))?$""".toRegex()
    val callWithArgumentsAndPathPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\(.+?\)(?:!!)?\??\.(?<path>$CHAIN)$""".toRegex()
}
