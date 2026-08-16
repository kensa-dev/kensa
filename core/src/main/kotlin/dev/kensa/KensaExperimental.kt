package dev.kensa

/**
 * Marks a Kensa API as experimental.
 *
 * Experimental APIs are shipped but **excluded from the 1.x semantic-versioning guarantee**: they may
 * change in source-incompatible ways, or be removed, in any 1.x release without a major-version bump.
 *
 * This marks features we are still designing and would like feedback on, such as the org-flow
 * annotations. It is deliberately distinct from [KensaInternalApi], which marks plumbing that is
 * public only because Kensa's own modules span Gradle module boundaries: an experimental API is one
 * you are invited to try, whereas an internal API is one you should not call at all. Opting in to one
 * marker does not opt you in to the other.
 *
 * Opt in by annotating the using declaration with `@OptIn(KensaExperimental::class)`, or propagate the
 * requirement by annotating it with `@KensaExperimental`.
 */
@RequiresOptIn(
    message = "This Kensa API is experimental: it is not covered by the 1.x semantic-versioning guarantee and may " +
        "change or be removed in any 1.x release. Opt in with @OptIn(KensaExperimental::class) to acknowledge this.",
    level = RequiresOptIn.Level.WARNING,
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
annotation class KensaExperimental
