package io.github.landerlyoung.jenny.provider

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.JennyParameter
import io.github.landerlyoung.jenny.utils.isStatic
import io.github.landerlyoung.jenny.utils.toJniReturnTypeString

internal class ParametersProvider {

    fun makeParameter(element: JennyElement, useJniHelper: Boolean, forceStatic: Boolean = false): String =
        buildString {
            if (!useJniHelper) {
                append("JNIEnv* env")
                if (element.isStatic() || forceStatic) {
                    append(", jclass clazz")
                } else {
                    append(", jobject thiz")
                }
            }
        }

    fun getJennyElementJniParams(
        element: JennyElement,
        useJniHelper: Boolean = false,
        forceStatic: Boolean = false
    ): String = buildString {
        append(makeParameter(element, useJniHelper, forceStatic))

        element.declaringClass?.let {
            if ((it as JennyClazzElement).isNestedClass) {
                append(it.type.toJniReturnTypeString())
                append(" ")
                append("enclosingClass")
            }
        }

        if (element is JennyExecutableElement)
            append(getJniParameters(element.parameters))
    }

    private fun getJniParameters(parameters: List<JennyParameter>): String = buildString {
        parameters.forEach { param ->
            append(", ")
            append(param.type.toJniReturnTypeString())
            append(' ')
            append(param.name)
        }
    }

    fun getJavaMethodParameters(method: JennyExecutableElement): String {
        return method.parameters
            .joinToString(", ") { param ->
                "${param.type.typeName} ${param.name}"
            }
    }

}