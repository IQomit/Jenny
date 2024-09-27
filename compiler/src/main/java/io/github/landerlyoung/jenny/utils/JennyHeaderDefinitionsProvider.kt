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

import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.element.model.type.JennyType
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.ProxyConfiguration
import io.github.landerlyoung.jenny.resolver.JennyMethodRecord
import io.github.landerlyoung.jenny.stripNonASCII

internal object JennyHeaderDefinitionsProvider {
    fun getHeaderInitForGlue(classInfo: ClassInfo): String {
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

    fun getConstantsDefinitions(constants: Collection<JennyVarElement>) = buildString {
        constants.forEach {
            append(getConstexprStatement(it))
        }
        append('\n')
    }

    private fun getConstexprStatement(property: JennyVarElement): String {
        val constValue = property.call()
        val jniType = property.type.toJniTypeString()
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

    fun getEndNameSpace(className: String, isSource: Boolean = false): String {
        val outputString = StringBuilder()

        outputString.append('\n')
        if (!isSource)
            outputString.append("} // endof namespace $className\n")
        return outputString.toString()
    }

    fun getMethodsDefinitions(
        classInfo: ClassInfo,
        methods: Collection<JennyExecutableElement>,
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
            val javaModifiers = method.modifiers.print()
            val javaReturnType = method.type.typeName
            val javaMethodName = method.name
            val javaParameters = getMethodParameters(method)
            val javaMethodSignature = getBinaryMethodSignature(method)
            val export = if (isSource) "" else "JNIEXPORT "
            val jniCall = if (isSource) "" else "JNICALL "
            val jniReturnType = method.type.toJniTypeString()
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

    private fun getNativeMethodParam(method: JennyExecutableElement): String {
        val sb = StringBuilder()
        sb.append("JNIEnv* env")

        val isStatic = JennyModifier.STATIC in method.modifiers

        if (isStatic) {
            sb.append(", jclass clazz")
        } else {
            sb.append(", jobject thiz")
        }
        // Add parameters from the function
        method.parameters.forEach { param ->
            sb.append(", ")
            sb.append(param.type.toJniTypeString())
            sb.append(' ')
            sb.append(param.name ?: "param")
        }
        return sb.toString()
    }

    private fun getMethodName(jniClassName: String, method: JennyExecutableElement): String {
        return "Java_" + jniClassName + "_" + method.name.replace("_", "_1").stripNonASCII()
    }

    private fun getBinaryMethodSignature(function: JennyExecutableElement): String {
        return Signature(function).toString()
    }

    private class Signature(
        private val function: JennyExecutableElement
    ) {

        private fun getSignatureClassName(type: JennyType): String {
            val output = StringBuilder()
            var jennyType = type

            while (jennyType.isArray()) {
                output.append('[')
                jennyType = (jennyType.componentType ?: error("Array type missing"))
            }

            when (jennyType.jennyKind) {
                JennyKind.BOOLEAN -> output.append('Z')
                JennyKind.BYTE -> output.append('B')
                JennyKind.CHAR -> output.append('C')
                JennyKind.SHORT -> output.append('S')
                JennyKind.INT -> output.append('I')
                JennyKind.LONG -> output.append('J')
                JennyKind.FLOAT -> output.append('F')
                JennyKind.DOUBLE -> output.append('D')
                JennyKind.VOID -> output.append('V')
                else -> output.append('L' + type.typeName.replace('.', '/')).append(';')
            }
            return output.toString()
        }

        override fun toString(): String = buildString {
            append('(')

            if (function.name.contentEquals("<init>")) {
                val clazz = function.declaringClass
                if ((clazz?.declaringClass as JennyClazzElement).isNestedClass) {
                    append(getSignatureClassName(clazz.type))
                }
            }
            for (param in function.parameters) {
                append(getSignatureClassName(param.type))
            }
            append(')')
            append(getSignatureClassName(function.returnType))
        }
    }

    private fun getMethodParameters(method: JennyExecutableElement): String {
        return method.parameters
            .joinToString(", ") { param ->
                "${param.type.typeName} ${param.name}"
            }
    }

    fun getJniRegister(methods: Collection<JennyExecutableElement>): String = buildString {
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

    private fun getJniNativeMethods(methods: Collection<JennyExecutableElement>) = buildString {
        methods.forEachIndexed { index, method ->
            val methodName = method.name
            val signature = getBinaryMethodSignature(method)
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


    fun getProxyHeaderInit(proxyConfiguration: ProxyConfiguration) = buildString {
        append(
            """
            |#pragma once
            |
            |#include <jni.h>
            |#include <assert.h>                        
            |""".trimMargin()
        )
        if (proxyConfiguration.threadSafe)
            append(
                """
                |#include <atomic>
                |#include <mutex>
                |
                |""".trimMargin()
            )

        if (proxyConfiguration.useJniHelper) {
            append(
                """
                |#include "jnihelper.h"
                |
                |""".trimMargin()
            )
        }
    }

    fun getProxyHeaderClazzInit() = buildString {
        append(
            """
            |
            |public:
            |
            |    static bool initClazz(JNIEnv* env);
            |    
            |    static void releaseClazz(JNIEnv* env);
            |
            |    static void assertInited(JNIEnv* env) {
            |        auto initClazzSuccess = initClazz(env);
            |        assert(initClazzSuccess);
            |    }
            |    
            |""".trimMargin()
        )
    }

    fun getMethodOverloadPostfix(method: JennyExecutableElement): String {
        val signature = getBinaryMethodSignature(method)
        val paramSig = signature.subSequence(signature.indexOf('(') + 1, signature.indexOf(")")).toString()
        return "__" + paramSig.replace("_", "_1")
            .replace("/", "_")
            .replace(";", "_2")
            .stripNonASCII()
    }

    fun getConstructorsDefinitions(
        simpleClassName: String,
        constructors: Collection<JennyMethodRecord>,
        useJniHelper: Boolean
    ) = buildString {
        val paramerter = makeParam(true,useJniHelper,)
        constructors.forEach { constructor ->
            append(
                """
                    |    // construct: ${constructor.method.modifiers.print()} ${simpleClassName}(${getMethodParameters(constructor.method)
                })
                    |    static ${constructor.method.type.typeName} newInstance${constructor.resolvedPostFix}(${param}) {
                    |        ${methodPrologue(true, useJniHelper)}
                    |        return env->NewObject(${mHelper.getClassState(mHelper.getClazz())}, ${
                    mHelper.getClassState(
                        mHelper.getConstructorName(constructor.index)
                    )
                }${mHelper.getJniMethodParamVal(mClazz, constructor.method, useJniHelper)});
                    |    }
                    |
                    |""".trimMargin()
            )
        }
        append('\n')
    }
}
//
//internal class MethodOverloadResolver(private val resolver: Resolver<JennyElement, String>) :
//    Resolver<List<JennyExecutableElement>, List<MethodRecord<JennyExecutableElement>>> {
//    override fun resolve(
//        input: List<JennyExecutableElement>
//    ): List<MethodRecord<JennyExecutableElement>> {
//        val duplicateRecord = mutableMapOf<String, Boolean>()
//        input.forEach {
//            val p = resolver.resolve(it)
//            duplicateRecord[p] = duplicateRecord.containsKey(p)
//        }
//
//        return input.mapIndexed { index, method ->
//            val p = resolver.resolve(method)
//            if (duplicateRecord[p]!! || Constants.CPP_RESERVED_WORS.contains(method.name)) {
//                MethodRecord(method, JennyHeaderDefinitionsProvider.getMethodOverloadPostfix(method), index)
//            } else {
//                MethodRecord(method, "", index)
//            }
//        }
//    }
//}