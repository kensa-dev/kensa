package dev.kensa.parse

import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor

data class MethodParameters(val descriptors: Map<String, ParameterAccessor>)