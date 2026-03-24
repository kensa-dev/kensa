package dev.kensa.sentence


fun TemplateToken.Type.asTemplateToken(template: String = "", vararg types: TemplateToken.Type) = TemplateToken.SimpleTemplateToken(template, setOf(this, *types))

fun aRenderedValueOf(value: String, cssClasses: Set<String> = emptySet(), hint: String? = null) = RenderedToken.RenderedValueToken(value, cssClasses, hint)
