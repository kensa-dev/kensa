package dev.kensa.parse

import dev.kensa.parse.Accessor.ParameterAccessor

data class MethodParameters(val descriptors: Map<String, ParameterAccessor>)