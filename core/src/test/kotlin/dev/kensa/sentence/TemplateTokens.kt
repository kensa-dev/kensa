package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor


fun TemplateToken.Type.asTemplateToken(template: String = "", emphasis: EmphasisDescriptor = EmphasisDescriptor.Default, vararg types: TemplateToken.Type) = TemplateToken.SimpleTemplateToken(template, emphasis, setOf(this, *types))

fun aRenderedValueOf(value: String, cssClasses: Set<String> = emptySet()) = RenderedToken.RenderedValueToken(value, cssClasses)
