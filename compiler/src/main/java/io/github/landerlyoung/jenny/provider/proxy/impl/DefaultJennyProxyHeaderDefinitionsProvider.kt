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

package io.github.landerlyoung.jenny.provider.proxy.impl

import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.generator.model.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.utils.Constants
import io.github.landerlyoung.jenny.utils.FieldSetterGetterFinder
import io.github.landerlyoung.jenny.utils.JennyNameProvider
import io.github.landerlyoung.jenny.utils.ParametersProvider
import io.github.landerlyoung.jenny.utils.isStatic
import io.github.landerlyoung.jenny.utils.needWrapLocalRef
import io.github.landerlyoung.jenny.utils.print
import io.github.landerlyoung.jenny.utils.toCamelCase
import io.github.landerlyoung.jenny.utils.toJniCall
import io.github.landerlyoung.jenny.utils.toJniReturnTypeString

internal class DefaultJennyProxyHeaderDefinitionsProvider : JennyProxyHeaderDefinitionsProvider {

    private val parametersProvider = ParametersProvider()

    override val autoGenerateNotice: String
        get() = Constants.AUTO_GENERATE_NOTICE

    override fun getProxyHeaderInit(
        jennyProxyConfiguration: JennyProxyConfiguration,
        startOfNamespace: String,
        classInfo: ClassInfo
    ) =
        buildString {
            append(
                """
            |#pragma once
            |
            |#include <jni.h>
            |#include <assert.h>                        
            |""".trimMargin()
            )
            if (jennyProxyConfiguration.threadSafe)
                append(
                    """
                |#include <atomic>
                |#include <mutex>
                |""".trimMargin()
                )

            if (jennyProxyConfiguration.useJniHelper) {
                append(
                    """
                |#include "jnihelper.h"
                |
                |""".trimMargin()
                )
            }
            append(
                """
                |${startOfNamespace}
                |class ${classInfo.cppClassName} {
                |
                |public:
                |    static constexpr auto FULL_CLASS_NAME = "${classInfo.slashClassName}";
                |
                |""".trimMargin()
            )
        }

    override fun getConstantsIdDeclare(constants: Collection<JennyVarElement>) = buildString {
        constants.forEach {
            append(parametersProvider.getConstexprStatement(it))
        }
        append('\n')
    }

    override fun getProxyHeaderClazzInit() = buildString {
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

    override fun getConstructorsDefinitions(
        simpleClassName: String,
        cppClassName: String,
        constructors: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ) = buildString {
        val returnType = if (useJniHelper) cppClassName else "jobject"

        constructors.forEach { (constructor, count) ->
            val jniParameters = parametersProvider.getJennyElementJniParams(element = constructor,useJniHelper)
            val javaParameters = parametersProvider.getJavaMethodParameters(constructor)
            val methodPrologue = getJniMethodPrologue(useJniHelper, isStatic = true)
            append(
                """
                    |    // construct: ${constructor.modifiers.print()} ${simpleClassName}($javaParameters)
                    |    static $returnType ${constructor.name}(${jniParameters}) {
                    |           $methodPrologue
                    |        return env->NewObject(${JennyNameProvider.getClassState()}, ${
                    JennyNameProvider.getClassState(JennyNameProvider.getConstructorName(count))
                }${parametersProvider.getJniMethodParamVal(constructor, useJniHelper)});
                    |    }
                    |
                    |""".trimMargin()
            )
        }
        append('\n')
    }

    private fun getJniMethodPrologue(
        useJniHelper: Boolean,
        isStatic: Boolean
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


    override fun getMethodsDefinitions(
        methods: Map<JennyExecutableElement, Int>,
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
            val jniParam = parametersProvider.getJennyElementJniParams(element = method,useJniHelper)
            val methodPrologue = getJniMethodPrologue(useJniHelper,isStatic)
            if (useJniHelper)
                append("    // for jni helper\n")

            append(
                """
                |    // method: ${method.modifiers.print()} ${method.returnType.typeName} ${method.name}(${
                    parametersProvider.getJavaMethodParameters(method)
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
            if (parametersProvider.returnTypeNeedCast(jniReturnType)) {
                append("reinterpret_cast<${jniReturnType}>(")
            }
            append(
                "env->Call${static}${method.returnType.toJniCall()}Method(${classOrObj}, ${
                    JennyNameProvider.getClassState(
                        JennyNameProvider.getElementName(method, count)
                    )
                }${parametersProvider.getJniMethodParamVal(method, useJniHelper)})"
            )
            if (parametersProvider.returnTypeNeedCast(jniReturnType)) {
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

    override fun getFieldsDefinitions(
        fields: Collection<JennyVarElement>,
        allMethods: Collection<JennyExecutableElement>,
        useJniHelper: Boolean,
        getterSetterForAllFields: Boolean,
        generateGetterForField: (JennyVarElement) -> Boolean,
        generateSetterForField: (JennyVarElement) -> Boolean
    ): String = buildString {

        fields.forEachIndexed { index, field ->
            val isStatic = field.isStatic()
            val camelCase = field.name.toCamelCase()
            val hasGetterSetter = FieldSetterGetterFinder.hasGetterSetter(
                field = field,
                allMethods = allMethods,
                generateGetterForFields = generateGetterForField(field),
                generateSetterForFields = generateSetterForField(field),
                allFields = getterSetterForAllFields
            )
            val fieldId = JennyNameProvider.getElementName(field, index)
            val typeForJniCall = field.type.toJniCall()
            val static = if (isStatic) "Static" else ""
            val staticMod = if (isStatic || !useJniHelper) "static " else ""
            val constMod = if (isStatic || !useJniHelper) "" else "const "
            val classOrObj = if (isStatic) JennyNameProvider.getClassState() else "thiz"
            val jniEnv = "env"
            val methodPrologue = getJniMethodPrologue(useJniHelper,isStatic)
            val jniReturnType = field.type.toJniReturnTypeString()
            var comment = "// field: ${field.modifiers.print()} ${field.type.typeName} ${field.name}"
            if (useJniHelper) {
                comment = "    // for jni helper\n    $comment"
            }
            if (hasGetterSetter.contains(FieldSetterGetterFinder.GetterSetter.GETTER)) {
                val parameters = parametersProvider.makeParameter(field, useJniHelper)
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

                if (parametersProvider.returnTypeNeedCast(jniReturnType)) {
                    append("reinterpret_cast<${jniReturnType}>(")
                }

                append(
                    "${jniEnv}->Get${static}${typeForJniCall}Field(${classOrObj}, ${
                        JennyNameProvider.getClassState(
                            fieldId
                        )
                    });"
                )
                append("\n}")
            }

            if (hasGetterSetter.contains(FieldSetterGetterFinder.GetterSetter.SETTER)) {
                val fieldJniType =
                    if (useJniHelper && field.type.needWrapLocalRef())
                        "::jenny::LocalRef<$jniReturnType>"
                    else
                        jniReturnType
                val parameter = parametersProvider.makeParameter(field, useJniHelper)

                val preparameters =  if(parameter.isEmpty()) parameter else "$parameter, "
                val parameters =  preparameters + fieldJniType+ " ${field.name}"
                val passedParam =
                    if (useJniHelper && field.type.needWrapLocalRef()) "${field.name}.get()" else field.name
                append(
                    """
                        |    
                        |    $comment
                        |    ${staticMod}void set${camelCase}(${parameters}) ${constMod}{
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


    override fun generateForJniHelper(cppClassName: String): String = buildString {
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
            |    ${cppClassName}(jobject ref, bool owned = false): _local(ref, owned) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |    ${cppClassName}(::jenny::LocalRef<jobject> ref): _local(std::move(ref)) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |    ${cppClassName}(::jenny::GlobalRef<jobject> ref): _global(std::move(ref)) {
            |       assertInited(::jenny::Env().get());
            |    }
            |   
            |""".trimMargin()
        )
    }

    override fun initPreDefinition(isThreadSafe: Boolean): String = buildString {
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

    override fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement, Int>): String = buildString {
        constructors.forEach { (_, count) ->
            append("    jmethodID ${JennyNameProvider.getConstructorName(count)} = nullptr;\n")
        }
        append('\n')
    }

    override fun getMethodIdDeclare(methods: Map<JennyExecutableElement, Int>): String = buildString {
        methods.forEach { (method, count) ->
            append("    jmethodID ${JennyNameProvider.getElementName(method, count)} = nullptr;\n")
        }
        append('\n')
    }

    override fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String = buildString {
        fields.forEachIndexed { index, field ->
            append("    jfieldID ${JennyNameProvider.getElementName(field, index)} = nullptr;\n")
        }
        append('\n')
    }

    override fun initPostDefinition(endNamespace: String): String = buildString {
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
        append(endNamespace)
        append("\n\n")
    }
}