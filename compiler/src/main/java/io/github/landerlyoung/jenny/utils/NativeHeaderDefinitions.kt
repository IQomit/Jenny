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

import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.stripNonASCII
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

internal object NativeHeaderDefinitions {
    fun getHeaderInit(classInfo: ClassInfo): String {
        return """
                |
                |/* C++ header file for class ${classInfo.slashClassName} */
                |#pragma once
                |
                |#include <jni.h>
                |
                |namespace ${classInfo.simpleClassName} {
                |
                |// DO NOT modify
                |static constexpr auto FULL_CLASS_NAME = u8"$${classInfo.slashClassName}";
                |
                |""".trimMargin()
    }

    fun getConstantsDefinitions(constants: Sequence<KProperty1<out Any, *>>): String {
        val outputString = StringBuilder()
        constants.forEach {
            outputString.append(getConstexprStatement(it))
        }
        return outputString.toString()
    }

    private fun getConstexprStatement(property: KProperty1<out Any, *>): String {
        val constValue = property.call() ?: throw IllegalArgumentException("Property has no constant value")
        val jniType = property.returnType.toJniTypeString()
        val nativeType = if (jniType == "jstring") "auto" else jniType

        val value = when (constValue) {
            is Boolean -> if (constValue) "JNI_TRUE" else "JNI_FALSE"
            is Number -> constValue.toString()
            is Char -> "'${constValue}'"
            is String -> "u8\"$constValue\""
            else -> throw IllegalArgumentException("Unknown type: $constValue (${constValue.javaClass})")
        }

        return "static constexpr $nativeType ${property.name} = $value;"
    }

    fun getEndNameSpace(className: String, isSource: Boolean = false): String {
        val outputString = StringBuilder()

        outputString.append('\n')
        if (!isSource)
            outputString.append("} // endof namespace $className\n")
        return outputString.toString()
    }

    fun getMethodsDefinitions(
        classInfo: ClassInfo,
        methods: Sequence<KFunction<*>>,
        isSource: Boolean = false
    ): String {
        val outputString = StringBuilder()
        if (!isSource) {
            outputString.append(
                """
                |
                |#ifdef __cplusplus
                |extern "C" {
                |#endif
                |
                |""".trimMargin()
            )
        }

        methods.forEach { method ->
            val javaModifiers = getModifiers(method)
            val javaReturnType = method.returnType.javaType.toString()
            val javaMethodName = method.javaMethod?.name ?: ""
            val javaParameters = getMethodParameters(method)
            val javaMethodSignature = getBinaryMethodSignature(method)
            val export = if (isSource) "" else "JNIEXPORT "
            val jniCall = if (isSource) "" else "JNICALL "
            val jniReturnType = method.returnType.toJniTypeString()
            val nativeMethodName =
                if (isSource)
                    classInfo.className + "::" + getMethodName(classInfo.jniClassName, method)
                else
                    getMethodName(classInfo.jniClassName, method)
            val nativeParameters = getNativeMethodParam(method)

            outputString.append(
                """
                    |/*
                    | * Class:     ${classInfo.className}
                    | * Method:    $javaModifiers $javaReturnType ${javaMethodName}(${javaParameters})
                    | * Signature: $javaMethodSignature
                    | */
                    |${export}${jniReturnType} ${jniCall}${nativeMethodName}(${nativeParameters})""".trimMargin()
            )

            if (isSource) {
                outputString.append(buildMethodBodyWithReturnStatement(method))
            } else {
                outputString.append(';')
            }
            outputString.append("\n\n")
        }
        return outputString.toString()
    }

    private fun buildMethodBodyWithReturnStatement(method: KFunction<*>): String {
        val outputString = StringBuilder()

        outputString.append(
            """
                |    // TODO(jenny): generated method stub.
                |
            """.trimMargin()
        )
        outputString.append("    ")
        outputString.append(getReturnStatement(method))
        outputString.append('\n')
        return outputString.toString()
    }

    private fun getReturnStatement(function: KFunction<*>): String {
        val returnType = function.returnType.javaType
        return buildString {
            if (returnType == Void.TYPE) {
                return@buildString // No return statement needed for void
            }

            append("return ")

            when (returnType) {
                java.lang.Boolean.TYPE -> append("JNI_FALSE") // Handle boolean
                Integer.TYPE, java.lang.Long.TYPE,
                java.lang.Short.TYPE, java.lang.Byte.TYPE,
                java.lang.Float.TYPE, java.lang.Double.TYPE,
                Character.TYPE -> append("0") // Handle numeric primitive types
                String::class.java -> append("env->NewStringUTF(\"Hello From Jenny\")") // Handle string return
                else -> append("nullptr") // Handle other objects
            }
            append(";")
        }
    }

    private fun getNativeMethodParam(method: KFunction<*>): String {
        val sb = StringBuilder()
        sb.append("JNIEnv* env")

        // Check if the method is static
        val isStatic = method.instanceParameter == null

        if (isStatic) {
            sb.append(", jclass clazz")
        } else {
            sb.append(", jobject thiz")
        }
        // Add parameters from the function
        method.parameters.drop(1).forEach { param ->
            sb.append(", ")
            sb.append(param.type.toJniTypeString())
            sb.append(' ')
            sb.append(param.name ?: "param")
        }
        return sb.toString()
    }

    private fun getMethodName(jniClassName: String, method: KFunction<*>): String {
        return "Java_" + jniClassName + "_" + method.name.replace("_", "_1").stripNonASCII()
    }

    private fun getBinaryMethodSignature(function: KFunction<*>): String {
        return Signature(function).toString()
    }

    private class Signature(
        private val function: KFunction<*>?
    ) {

        private fun StringBuilder.getSignatureClassName(type: kotlin.reflect.KType) {
            var t = type
            while (t.classifier is kotlin.reflect.KClass<*> && t.classifier == Array::class) {
                append('[')
                t = (t.arguments.firstOrNull()?.type ?: error("Array type missing"))
            }
            val jvmName = t.classifier?.javaClass?.name
            if (jvmName != null) {
                when (jvmName) {
                    "java.lang.Boolean" -> append('Z')
                    "java.lang.Byte" -> append('B')
                    "java.lang.Character" -> append('C')
                    "java.lang.Short" -> append('S')
                    "java.lang.Integer" -> append('I')
                    "java.lang.Long" -> append('J')
                    "java.lang.Float" -> append('F')
                    "java.lang.Double" -> append('D')
                    "java.lang.Void" -> append('V')
                    else -> append('L').append(jvmName.replace('.', '/')).append(';')
                }
            } else {
                append("unknown")
            }
        }

        override fun toString(): String = buildString {
            if (function != null) {
                append('(')
                for (param in function.valueParameters) {
                    getSignatureClassName(param.type)
                }
                append(')')
                getSignatureClassName(function.returnType)
            } else {
                append("unknown")
            }
        }
    }

    private fun getMethodParameters(method: KFunction<*>): String {
        return method.parameters
            .drop(1) // Drop the first parameter which is the receiver (for member functions)
            .joinToString(", ") { param ->
                val paramType = param.type.toString() // Get the type of the parameter
                val paramName = param.name ?: "unknown" // Get the name of the parameter
                "$paramType $paramName"
            }
    }

    private fun getModifiers(method: KFunction<*>): String {
        val javaMethod = method.javaMethod ?: return ""

        return sequenceOf(
            Modifier.isPublic(javaMethod.modifiers) to "public",
            Modifier.isProtected(javaMethod.modifiers) to "protected",
            Modifier.isPrivate(javaMethod.modifiers) to "private",
            Modifier.isFinal(javaMethod.modifiers) to "final",
            Modifier.isStatic(javaMethod.modifiers) to "static",
            Modifier.isAbstract(javaMethod.modifiers) to "abstract",
            Modifier.isSynchronized(javaMethod.modifiers) to "synchronized"
        ).filter { it.first }
            .map { it.second }
            .sorted()
            .joinToString(" ") { it.lowercase(Locale.US) }
    }
}