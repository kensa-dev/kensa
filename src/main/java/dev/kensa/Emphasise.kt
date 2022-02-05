package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Retention(RUNTIME)
@Target(FUNCTION, FIELD, VALUE_PARAMETER)
annotation class Emphasise(val textStyles: Array<TextStyle> = [TextStyle.TextWeightNormal], val textColour: Colour = Colour.Default, val backgroundColor: Colour = Colour.Default)