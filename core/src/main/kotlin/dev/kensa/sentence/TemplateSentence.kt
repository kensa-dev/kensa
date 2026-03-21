package dev.kensa.sentence

class TemplateSentence(val tokens: List<TemplateToken>, val lineNumber: Int = 0)

class RenderedSentence(val tokens: List<RenderedToken>, val lineNumber: Int)