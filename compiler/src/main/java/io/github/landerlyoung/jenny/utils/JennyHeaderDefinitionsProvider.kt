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
import io.github.landerlyoung.jenny.element.model.type.JennyType
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.ProxyConfiguration
import io.github.landerlyoung.jenny.stripNonASCII
import java.util.*

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

    fun getConstantsIdDeclare(constants: Collection<JennyVarElement>) = buildString {
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

    fun getEndNameSpace(className: String, isSource: Boolean = false): String {
        val outputString = StringBuilder()

        outputString.append('\n')
        if (!isSource)
            outputString.append("} // endof namespace $className\n")
        return outputString.toString()
    }

    fun getNativeMethodsDefinitions(
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
            val javaParameters = getJavaMethodParameters(method)
            val javaMethodSignature = Signature.getBinaryJennyElementSignature(method)
            val export = if (isSource) "" else "JNIEXPORT "
            val jniCall = if (isSource) "" else "JNICALL "
            val jniReturnType = method.type.toJniReturnTypeString()
            val nativeMethodName =
                if (isSource)
                    classInfo.className + "::" + getNativeMethodName(classInfo.jniClassName, method)
                else
                    getNativeMethodName(classInfo.jniClassName, method)
            val nativeParameters = getJennyElementJniParams(element = method)

            outputString.append(
                """
                    |/*
                    | * Class:     ${classInfo.className}
                    | * Method:    $javaModifiers $javaReturnType $javaMethodName($javaParameters)
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

    private fun getJennyElementJniParams(
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

    private fun makeParameter(element: JennyElement, useJniHelper: Boolean, forceStatic: Boolean = false): String =
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

    private fun getJniParameters(parameters: List<JennyParameter>): String = buildString {
        parameters.forEach { param ->
            append(", ")
            append(param.type.toJniReturnTypeString())
            append(' ')
            append(param.name)
        }
    }

    private fun getJniMethodParamVal(
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

    private fun JennyType.needWrapLocalRef(): Boolean {
        return (!isPrimitive() && jennyKind != JennyKind.VOID)
    }

    private fun getNativeMethodName(jniClassName: String, method: JennyExecutableElement): String {
        return "Java_" + jniClassName + "_" + method.name.replace("_", "_1").stripNonASCII()
    }

    private fun getJavaMethodParameters(method: JennyExecutableElement): String {
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

    fun getProxyHeaderInit(proxyConfiguration: ProxyConfiguration, classInfo: ClassInfo) = buildString {
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
        append(
            """
                |class ${classInfo.simpleClassName}Proxy {
                |
                |public:
                |    static constexpr auto FULL_CLASS_NAME = "${classInfo.slashClassName}";
                |
                |""".trimMargin()
        )
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
        val signature = Signature.getBinaryJennyElementSignature(method)
        val paramSig = signature.subSequence(signature.indexOf('(') + 1, signature.indexOf(")")).toString()
        return "__" + paramSig.replace("_", "_1")
            .replace("/", "_")
            .replace(";", "_2")
            .stripNonASCII()
    }

    fun getConstructorsDefinitions(
        simpleClassName: String,
        constructors: Map<JennyExecutableElement,Int>,
        useJniHelper: Boolean
    ) = buildString {
        constructors.forEach { (constructor, count) ->
            val jniParameters = getJennyElementJniParams(element = constructor, forceStatic = true)
            val javaParameters = getJavaMethodParameters(constructor)
            val methodPrologue = getMethodPrologue(useJniHelper, isStatic = true)
            append(
                """
                    |    // construct: ${constructor.modifiers.print()} ${simpleClassName}($javaParameters)
                    |    static ${constructor.type.typeName} newInstance${constructor.name}(${jniParameters}) {
                    |           $methodPrologue
                    |        return env->NewObject(${JennyNameProvider.getClassState()}, ${
                    JennyNameProvider.getClassState(JennyNameProvider.getConstructorName(count))
                }${getJniMethodParamVal(constructor, useJniHelper)});
                    |    }
                    |
                    |""".trimMargin()
            )
        }
        append('\n')
    }


    private fun getMethodPrologue(
        useJniHelper: Boolean,
        isStatic: Boolean = true,
    ): String {
        return if (useJniHelper) {
            if (isStatic) {
                "::jenny::Env env; assertInited(env.get());"
            } else {
                "::jenny::Env env; ::jenny::LocalRef<jobject> jennyLocalRef = getThis(false); jobject thiz = jennyLocalRef.get();"
            }
        } else {
            "assertInited(env);"
        }
    }

    fun getMethodsDefinitions(
        methods: Map<JennyExecutableElement,Int>,
        useJniHelper: Boolean
    ) = buildString {
        methods.forEach { (method, count) ->
            val isStatic = method.isStatic()
            val jniReturnType = method.returnType.toJniReturnTypeString()
            val functionReturnType =
                if (useJniHelper && method.returnType.needWrapLocalRef())
                    "::jenny::LocalRef<$jniReturnType>"
                else
                    jniReturnType
            val staticMod = if (isStatic || !useJniHelper) "static " else ""
            val constMod = if (isStatic || !useJniHelper) "" else "const "
            val classOrObj = if (isStatic) JennyNameProvider.getClassState() else "thiz"
            val static = if (isStatic) "Static" else ""
            val callExpressionClosing: StringBuilder = StringBuilder()
            if (returnTypeNeedCast(jniReturnType)) {
                callExpressionClosing.append(")")
            }
            if (useJniHelper && method.returnType.needWrapLocalRef()) {
                callExpressionClosing.append(")")
            }
            callExpressionClosing.append(";")
            val jniParam = getJennyElementJniParams(element = method)
            val methodPrologue = getMethodPrologue(useJniHelper)
            if (useJniHelper)
                append("    // for jni helper\n")

            append(
                """
                |    // method: ${method.modifiers.print()} ${method.returnType.typeName} ${method.name}(${
                    getJavaMethodParameters(
                        method
                    )
                })
                |    ${staticMod}${functionReturnType} ${method.name}(${jniParam}) ${constMod}{
                |        $methodPrologue
                |""".trimMargin()
            )

            if (method.returnType.jennyKind != JennyKind.VOID) {
                append("        return ")
            } else {
                append("        ")
            }
            if (useJniHelper && method.returnType.needWrapLocalRef()) {
                append(functionReturnType).append("(")
            }
            if (returnTypeNeedCast(jniReturnType)) {
                append("reinterpret_cast<${jniReturnType}>(")
            }
            append(
                "env->Call${static}${method.returnType.toJniCall()}Method(${classOrObj}, ${
                    JennyNameProvider.getClassState(
                        JennyNameProvider.getElementName(method, count)
                    )
                }${getJniMethodParamVal(method, useJniHelper)})"
            )
            if (returnTypeNeedCast(jniReturnType)) {
                append(")")
            }
            if (useJniHelper && method.returnType.needWrapLocalRef()) {
                append(")")
            }

            append(";\n")
            append("    }\n\n")
            append('\n')

        }
    }

    fun getFieldsDefinitions(
        fields: Collection<JennyVarElement>,
        allMethods: Collection<JennyExecutableElement>,
        useJniHelper: Boolean,
        getterSetterForAllFields: Boolean,
        generateGetterForFields: Boolean,
        generateSetterForFields: Boolean
    ): String = buildString {
        fields.forEachIndexed { index, field ->
            val isStatic = field.isStatic()
            val camelCase = field.name.toCamelCase()
            val hasGetterSetter = hasGetterSetter(
                field = field,
                allMethods = allMethods,
                generateGetterForFields = generateGetterForFields,
                generateSetterForFields = generateSetterForFields,
                allFields = getterSetterForAllFields
            )
            val fieldId = JennyNameProvider.getElementName(field, index)
            val typeForJniCall = field.type.toJniCall()
            val static = if (isStatic) "Static" else ""
            val staticMod = if (isStatic || !useJniHelper) "static " else ""
            val constMod = if (isStatic || !useJniHelper) "" else "const "
            val classOrObj = if (isStatic) JennyNameProvider.getClassState() else "thiz"
            val jniEnv = "env"
            val methodPrologue = getMethodPrologue(useJniHelper)
            val jniReturnType = field.type.toJniReturnTypeString()
            var comment = "// field: ${field.modifiers.print()} ${field.type.typeName} ${field.name}"
            if (useJniHelper) {
                comment = "    // for jni helper\n    $comment"
            }
            if (hasGetterSetter.contains(GetterSetter.GETTER)) {
                val parameters = makeParameter(field, useJniHelper)
                append(
                    """
                        |    $comment
                        |    ${staticMod}$jniReturnType get${camelCase}(${parameters}) ${constMod}{
                        |       $methodPrologue
                        |       return """.trimMargin()
                )
                if (useJniHelper && field.type.needWrapLocalRef()) {
                    append(jniReturnType).append("(")
                }

                if (returnTypeNeedCast(jniReturnType)) {
                    append("reinterpret_cast<${jniReturnType}>(")
                }

                append(
                    "${jniEnv}->Get${static}${typeForJniCall}Field(${classOrObj}, ${
                        JennyNameProvider.getClassState(
                            fieldId
                        )
                    })"
                )
            }

            if (hasGetterSetter.contains(GetterSetter.SETTER)) {
                val fieldJniType =
                    if (useJniHelper && field.type.needWrapLocalRef())
                        "::jenny::LocalRef<$jniReturnType>"
                    else
                        jniReturnType
                val param = makeParameter(field, useJniHelper) + fieldJniType
                val passedParam =
                    if (useJniHelper && field.type.needWrapLocalRef()) "${field.name}.get()" else field.name
                append(
                    """
                        |    $comment
                        |    ${staticMod}void set${camelCase}(${param}) ${constMod}{
                        |        $methodPrologue
                        |        ${jniEnv}->Set${static}${typeForJniCall}Field(${classOrObj}, ${
                        JennyNameProvider.getClassState(fieldId)
                    }, ${passedParam});
                        |    }
                        |
                        |""".trimMargin()
                )
                append('\n')
            }
        }
    }

    private fun returnTypeNeedCast(jniReturnType: String): Boolean {
        return when (jniReturnType) {
            "jclass", "jstring", "jarray", "jobjectArray",
            "jbooleanArray", "jbyteArray", "jcharArray",
            "jshortArray", "jintArray", "jlongArray",
            "jfloatArray", "jdoubleArray",
            "jthrowable", "jweak" -> true

            else -> false
        }
    }

    private fun hasGetterSetter(
        field: JennyElement,
        allMethods: Collection<JennyExecutableElement>,
        generateGetterForFields: Boolean,
        generateSetterForFields: Boolean,
        allFields: Boolean
    ): EnumSet<GetterSetter> {

        if (field.isConstant())
            return EnumSet.noneOf(GetterSetter::class.java)
        if (allFields && !(generateSetterForFields || generateGetterForFields))
            return addAutoGeneratedAccessors(allMethods = allMethods.map { it.name }.toSet(), field)

        val result = EnumSet.noneOf(GetterSetter::class.java)
        if (generateGetterForFields)
            result.add(GetterSetter.GETTER)
        if (generateSetterForFields)
            result.add(GetterSetter.SETTER)
        return result
    }

    private fun addAutoGeneratedAccessors(
        allMethods: Collection<String>,
        field: JennyElement
    ): EnumSet<GetterSetter> {
        val result = EnumSet.noneOf(GetterSetter::class.java)
        val camelCaseName = field.name.toCamelCase()
        val type = field.type.toJniReturnTypeString()
        if (shouldAddGetter(allMethods, camelCaseName, type))
            result.add(GetterSetter.GETTER)
        if (shouldAddSetter(allMethods, camelCaseName))
            result.add(GetterSetter.SETTER)
        return result
    }

    private fun shouldAddGetter(
        allMethods: Collection<String>,
        camelCaseName: String,
        type: String
    ): Boolean {
        return !allMethods.contains("get$camelCaseName") &&
                !(type == "jboolean" && allMethods.contains("is$camelCaseName"))
    }

    private fun shouldAddSetter(allMethods: Collection<String>, camelCaseName: String): Boolean {
        return !allMethods.contains("set$camelCaseName")
    }

    fun generateForJniHelper(simpleClassName: String): String = buildString {
        append(
            """
            |    // ====== jni helper ======
            |private:
            |    ::jenny::LocalRef<jobject> _local;
            |    ::jenny::GlobalRef<jobject> _global;
            | 
            |public:
            |
            |    // jni helper
            |    ::jenny::LocalRef<jobject> getThis(bool owned = true) const {
            |        if (_local) {
            |            if (owned) {
            |                return _local;
            |            } else {
            |                return ::jenny::LocalRef<jobject>(_local.get(), false);
            |            }
            |        } else {
            |            return _global.toLocal();
            |        }
            |    }
            |
            |    // jni helper constructors
            |    ${simpleClassName}Proxy(jobject ref, bool owned = false): _local(ref, owned) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |    ${simpleClassName}Proxy(::jenny::LocalRef<jobject> ref): _local(std::move(ref)) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |    ${simpleClassName}Proxy(::jenny::GlobalRef<jobject> ref): _global(std::move(ref)) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |""".trimMargin()
        )
    }

    fun initPreDefinition(isThreadSafe: Boolean): String = buildString {
        append(
            """
                |
                |private:
                |    struct ClassInitState {
                |
                        """.trimMargin()
        )

        if (isThreadSafe) {
            append(
                """
                 |    // thread safe init
                 |    std::atomic_bool sInited {};
                 |    std::mutex sInitLock {};
                 |""".trimMargin()
            )
        } else {
            append("    bool sInited = false;\n")
        }
        append(
            """
             |
             |    jclass sClazz = nullptr;
             |
             """.trimMargin()
        )

    }

    fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement,Int>): String = buildString {
        constructors.forEach { (_,count) ->
            append("    jmethodID ${JennyNameProvider.getConstructorName(count)} = nullptr;\n")
        }
        append('\n')
    }

    fun getMethodIdDeclare(methods: Map<JennyExecutableElement,Int>): String = buildString {
        methods.forEach { (method,count) ->
            append("    jmethodID ${JennyNameProvider.getElementName(method, count)} = nullptr;\n")
        }
        append('\n')
    }

    fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String = buildString {
        fields.forEachIndexed { index, field ->
            append("    jfieldID ${JennyNameProvider.getElementName(field, index)} = nullptr;\n")
        }
        append('\n')
    }

    fun initPostDefinition(): String = buildString {
        append(
            """
              |    }; // endof struct ClassInitState
              |
              |    static inline ClassInitState& getClassInitState() {
              |        static ClassInitState classInitState;
              |        return classInitState;
              |    }
              |
              |
                        """.trimMargin()
        )

        append("};\n")
        append("\n\n")
    }

    private enum class GetterSetter {
        GETTER, SETTER
    }
}