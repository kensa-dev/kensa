package dev.kensa

import dev.kensa.state.SetupStrategy
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class Notes(val value: String)

@Retention(RUNTIME)
@Target(FUNCTION, FIELD, VALUE_PARAMETER)
annotation class Emphasise(val textStyles: Array<TextStyle> = [TextStyle.TextWeightNormal], val textColour: Colour = Colour.Default, val backgroundColor: Colour = Colour.Default)

@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER)
annotation class Highlight(val value: String = "")

@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class Issue(vararg val value: String)

@Retention(RUNTIME)
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class NestedSentence

@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER)
annotation class ResolveHolder

@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER)
annotation class Resolve

@Retention(RUNTIME)
@Target(CLASS)
annotation class Sources(vararg val value: KClass<*>)

@Retention(RUNTIME)
@Target(VALUE_PARAMETER)
annotation class ParameterizedTestDescription

@Retention(RUNTIME)
@Target(FUNCTION, CLASS)
annotation class AutoOpenTab(val value: Tab)

@Retention(RUNTIME)
@Target(FUNCTION, CLASS)
annotation class UseSetupStrategy(val value: SetupStrategy)

