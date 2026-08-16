package dev.kensa

/**
 * Marks a Kensa API as internal implementation detail.
 *
 * These declarations are `public` only because Kensa's framework adapters and the Kotlin compiler
 * plugin live in separate Gradle modules and cannot see `internal`. They are **not** part of
 * the supported API, are excluded from the semantic-versioning guarantee, and may change or be removed
 * in any release.
 *
 * This is distinct from [KensaExperimental]: an experimental API is one we are still designing and
 * would like feedback on, whereas an internal API is one you should not call at all.
 *
 * If you find yourself needing to opt in to this marker in a test suite, something is missing from the
 * supported API. Please open an issue rather than depending on these declarations.
 */
@RequiresOptIn(
    message = "This is Kensa internal API: it is public only because Kensa's own modules need it, it is not " +
        "covered by the semantic-versioning guarantee, and it may change or be removed in any release.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS,
)
annotation class KensaInternalApi
