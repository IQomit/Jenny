/**
 * Copyright (C) 2024 The Qt Company Ltd.
 * Copyright 2016 landerlyoung@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    ): String = buildString {
        if (!useJniHelper) {
            append("JNIEnv* env")
            val isConstructor = if (element is JennyExecutableElement) element.isConstructor() else false
            if (!isConstructor) {
                if (element.isStatic()) {
                    append(", jclass clazz")
                } else {
                    append(", jobject thiz")
                }
            }
        }
    }

    fun getJennyElementJniParams(
        element: JennyElement,
        useJniHelper: Boolean,
    ): String = buildString {
        val params = mutableListOf<String>()

        val baseParams = makeParameter(element, useJniHelper)
        if (baseParams.isNotEmpty()) {
            params.add(baseParams)
        }

        element.declaringClass?.let {
            val clazzElement = it as? JennyClazzElement
            if (clazzElement?.isNestedClass == true) {
                params.add("${clazzElement.type.toJniReturnTypeString()} enclosingClass")
            }
        }

        if (element is JennyExecutableElement) {
            val execParams = getJniParameters(element.parameters, useJniHelper)
            if (execParams.isNotEmpty()) {
                params.add(execParams)
            }
        }

        append(params.joinToString(", "))
    }

    private fun getJniParameters(parameters: List<JennyParameter>, useJniHelper: Boolean): String =
        buildString {
            parameters.forEach { param ->
                if (isNotEmpty()) append(", ")
                append(param.type.toJniTypeString(useJniHelper))
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