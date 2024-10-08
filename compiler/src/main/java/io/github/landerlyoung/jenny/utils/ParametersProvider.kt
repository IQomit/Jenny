package io.github.landerlyoung.jenny.utils

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.JennyParameter
import io.github.landerlyoung.jenny.element.model.type.JennyKind

class ParametersProvider {

    fun makeParameter(
        element: JennyElement,
        useJniHelper: Boolean,
        forceStatic: Boolean = false
    ): String =
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

    fun getJniMethodParamVal(
        method: JennyExecutableElement,
        useJniHelper: Boolean = false,
    ): String = buildString {
        method.declaringClass?.let {
            if ((it as JennyClazzElement).isNestedClass) {
                append(", ")
                append("enclosingClass")
                if (useJniHelper)
                    append(".get()")
            }
        }

        method.parameters.forEach { param ->
            append(", ")
            append(param.name)
            if (useJniHelper && param.type.needWrapLocalRef()) {
                append(".get()")
            }
        }
    }

    fun getConstexprStatement(property: JennyVarElement): String {
        val constValue = try {
            property.call()
        } catch (e: Exception) {
            println("Warning: Failed to retrieve constant value for property '${property.name}': ${e.message}")
            when (property.type.jennyKind) {
                JennyKind.BOOLEAN -> false
                JennyKind.BYTE, JennyKind.INT -> 0
                JennyKind.SHORT -> 0.toShort()
                JennyKind.LONG -> 0L
                JennyKind.FLOAT -> 0f
                JennyKind.CHAR -> '\u0000'
                JennyKind.DOUBLE -> 0.0
                else -> "/* Unknown type for '${property.name}' */" // Default for unknown types
            }
        }
        val jniType = property.type.toJniReturnTypeString()
        val nativeType = if (jniType == "jstring") "auto" else jniType

        val value = when (constValue) {
            is Boolean -> if (constValue) "JNI_TRUE" else "JNI_FALSE"
            is Number -> constValue.toString()
            is Char -> "'${constValue}'"
            is String -> "u8\"$constValue\""
            else -> "Unknown type: $constValue (${constValue?.javaClass})"
        }
        return "static constexpr $nativeType ${property.name} = $value;\n"
    }

    fun returnTypeNeedCast(jniReturnType: String): Boolean {
        return when (jniReturnType) {
            "jclass", "jstring", "jarray", "jobjectArray",
            "jbooleanArray", "jbyteArray", "jcharArray",
            "jshortArray", "jintArray", "jlongArray",
            "jfloatArray", "jdoubleArray",
            "jthrowable", "jweak" -> true

            else -> false
        }
    }
}