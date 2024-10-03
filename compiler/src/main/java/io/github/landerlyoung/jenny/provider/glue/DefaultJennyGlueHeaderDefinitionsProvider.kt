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

package io.github.landerlyoung.jenny.provider.glue

import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.provider.ParametersProvider
import io.github.landerlyoung.jenny.utils.JennyNameProvider
import io.github.landerlyoung.jenny.utils.Signature
import io.github.landerlyoung.jenny.utils.print
import io.github.landerlyoung.jenny.utils.toJniReturnTypeString

internal class DefaultJennyGlueHeaderDefinitionsProvider : JennyGlueHeaderDefinitionsProvider {
    private val parametersProvider = ParametersProvider()

    override fun getHeaderInitForGlue(classInfo: ClassInfo, startOfNamespace: String): String {
        return """
                |
                |/* C++ header file for class ${classInfo.slashClassName} */
                |#pragma once
                |
                |#include <jni.h>
                |
                |${startOfNamespace}
                |namespace ${classInfo.simpleClassName} {
                |
                |// DO NOT modify
                |static constexpr auto FULL_CLASS_NAME = u8"$${classInfo.slashClassName}";
                |
                |""".trimMargin()
    }

    override fun getConstantsIdDeclare(constants: Collection<JennyVarElement>) = buildString {
        constants.forEach {
            append(getConstexprStatement(it))
        }
        append('\n')
    }

    private fun getConstexprStatement(property: JennyVarElement): String {
        val constValue = property.call()
        val jniType = property.type.toJniReturnTypeString()
        val nativeType = if (jniType == "jstring") "auto" else jniType

        val value = when (constValue) {
            is Boolean -> if (constValue) "JNI_TRUE" else "JNI_FALSE"
            is Number -> constValue.toString()
            is Char -> "'${constValue}'"
            is String -> "u8\"$constValue\""
            else -> throw IllegalArgumentException("Unknown type: $constValue (${constValue?.javaClass})")
        }

        return "static constexpr $nativeType ${property.name} = $value;"
    }

    override fun getEndNameSpace(className: String, endNamespace: String, isSource: Boolean) = buildString {
        append('\n')
        if (!isSource)
            append("} // endof namespace $className\n")
        append("${endNamespace}\n\n")
    }

    override fun getNativeMethodsDefinitions(
        classInfo: ClassInfo,
        methods: Collection<JennyExecutableElement>,
        isSource: Boolean
    ): String = buildString {
        if (!isSource) {
            append(
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
            val javaModifiers = method.modifiers.print()
            val javaReturnType = method.type.typeName
            val javaMethodName = method.name
            val javaParameters = parametersProvider.getJavaMethodParameters(method)
            val javaMethodSignature = Signature.getBinaryJennyElementSignature(method)
            val export = if (isSource) "" else "JNIEXPORT "
            val jniCall = if (isSource) "" else "JNICALL "
            val jniReturnType = method.type.toJniReturnTypeString()
            val nativeMethodName =
                if (isSource)
                    classInfo.simpleClassName + "::" + JennyNameProvider.getNativeMethodName(
                        classInfo.jniClassName,
                        method
                    )
                else
                    JennyNameProvider.getNativeMethodName(classInfo.jniClassName, method)
            val nativeParameters = parametersProvider.getJennyElementJniParams(element = method)

            append(
                """
                    |/*
                    | * Class:     ${classInfo.className}
                    | * Method:    $javaModifiers $javaReturnType $javaMethodName($javaParameters)
                    | * Signature: $javaMethodSignature
                    | */
                    |${export}${jniReturnType} ${jniCall}${nativeMethodName}(${nativeParameters})""".trimMargin()
            )

            if (isSource) {
                append(buildMethodBodyWithReturnStatement(method))
            } else {
                append(';')
            }
            append("\n\n")
        }
        if (!isSource) {
            append(
                """|#ifdef __cplusplus
                   |}
                   |#endif"""
                    .trimMargin()
            )
        }
    }


    override fun getJniRegister(methods: Collection<JennyExecutableElement>): String = buildString {
        append(
            """
            |/**
            |* register Native functions
            |* @returns success or not
            |*/
            |inline bool registerNativeFunctions(JNIEnv* env) {
            |// 1. C++20 has u8"" string as char8_t type, we should cast them.
            |// 2. jni.h has JNINativeMethod::name as char* type not const char*. (while Android does)
            |#define jenny_u8cast(u8) const_cast<char *>(reinterpret_cast<const char *>(u8))
            |   const JNINativeMethod gsNativeMethods[] = {
            |""".trimMargin()
        )
        append(getJniNativeMethods(methods))
        append(
            """
            |   };
            |
            |   const int gsMethodCount = sizeof(gsNativeMethods) / sizeof(JNINativeMethod);
            |
            |   bool success = false;
            |   jclass clazz = env->FindClass(jenny_u8cast(FULL_CLASS_NAME));
            |   if (clazz != nullptr) {
            |       success = !env->RegisterNatives(clazz, gsNativeMethods, gsMethodCount);
            |       env->DeleteLocalRef(clazz);
            |   }
            |   return success;
            |#undef jenny_u8cast
            |}
            |""".trimMargin()
        )
    }

    private fun buildMethodBodyWithReturnStatement(method: JennyExecutableElement) = buildString {
        append(" {\n")
        append(
            """
                |    // TODO(jenny): generated method stub.
                |
            """.trimMargin()
        )
        append("    ")
        append(getReturnStatement(method))
        append('\n')
        append("}")
    }

    private fun getReturnStatement(function: JennyExecutableElement): String {
        val returnType = function.returnType
        return buildString {
            if (returnType.jennyKind == JennyKind.VOID) {
                return@buildString // No return statement needed for void
            }

            append("return ")

            when (returnType.jennyKind) {
                JennyKind.BOOLEAN -> append("JNI_FALSE")
                JennyKind.INT, JennyKind.LONG,
                JennyKind.SHORT, JennyKind.BYTE,
                JennyKind.FLOAT, JennyKind.DOUBLE,
                JennyKind.CHAR -> append("0")

                else -> append("nullptr")
            }
            append(";")
        }
    }

    private fun getJniNativeMethods(methods: Collection<JennyExecutableElement>) = buildString {
        methods.forEachIndexed { index, method ->
            val methodName = method.name
            val signature = Signature.getBinaryJennyElementSignature(method)
            append(
                """
            |       {
            |           /* method name      */ jenny_u8cast(u8"$methodName"),
            |           /* method signature */ jenny_u8cast(u8"$signature"),
            |           /* function pointer */ reinterpret_cast<void *>($methodName)
            |       }""".trimMargin()
            )
            if (index < methods.size - 1) {
                append(",")
            }
            append('\n')
        }
    }
}