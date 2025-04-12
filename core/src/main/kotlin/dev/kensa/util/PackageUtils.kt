package dev.kensa.util

typealias PackageElements = List<String>

fun findCommonPackage(packageNames: List<String>): String {
    require(packageNames.isNotEmpty())

    return packageNames
        .map { it.toPackageElements() }
        .reduce(PackageElements::commonBaseWith)
        .joinToString(".")
}

private fun String.toPackageElements(): PackageElements = split(".")

private fun PackageElements.commonBaseWith(other: PackageElements): PackageElements = 
    zip(other)
        .takeWhile { pairAtIndex -> pairAtIndex.first == pairAtIndex.second }
        .map { entry -> entry.first }
