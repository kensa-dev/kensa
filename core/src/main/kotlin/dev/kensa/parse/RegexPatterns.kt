package dev.kensa.parse

internal object RegexPatterns {
    val chainedCallPattern: Regex = """^(\w+)(\(\))?(?:\.(\w+(?:\(\))?(?:\.\w+(?:\(\))?)*))?$""".toRegex()
    val fixturesPattern: Regex = """^fixtures[\[({](?:\w+\.)*(\w+)[\])}](?:\.(\w+(?:\(\))?(?:\.\w+(?:\(\))?)*))?$""".toRegex()
    val outputsByNamePattern: Regex = """^outputs[\[(](?:\w+\.)*(\w+)[])](?:\.(\w+(?:\(\))?(?:\.\w+(?:\(\))?)*))?$""".toRegex()
    val outputsByKeyPattern: Regex = """^outputs\("([a-zA-Z0-9_]+)"\)(?:\.(\w+(?:\(\))?(?:\.\w+(?:\(\))?)*))?$""".toRegex()
    val singleCallWithArgumentsPattern: Regex = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\(.*\)$""".toRegex()
}
