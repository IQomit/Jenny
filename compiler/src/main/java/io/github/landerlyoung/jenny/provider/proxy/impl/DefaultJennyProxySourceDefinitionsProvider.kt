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
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.utils.Constants
import io.github.landerlyoung.jenny.utils.JennyNameProvider
import io.github.landerlyoung.jenny.utils.Signature
import io.github.landerlyoung.jenny.utils.isStatic


internal class DefaultJennyProxySourceDefinitionsProvider : JennyProxySourceDefinitionsProvider {
    override val autoGenerateNotice: String
        get() = Constants.AUTO_GENERATE_NOTICE

    override fun generateSourcePreContent(
        headerFileName: String,
        startOfNamespace: String,
        cppClassName: String,
        errorLoggerFunction:String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String = buildString {
        if(!headerOnly)
            append(
                """
                |#include "$headerFileName"
                |
                |""".trimMargin()
            )
        if (errorLoggerFunction.isNotEmpty()) {
            append(
                """
                   |
                   |void ${errorLoggerFunction}(JNIEnv* env, const char* error);
                   |
                   |""".trimMargin()
            )
        }

        append(startOfNamespace)
        append("\n\n")
        val prefix = if (headerOnly) "/*static*/ inline" else "/*static*/"
        append(
            """
                |${prefix} bool ${cppClassName}::initClazz(JNIEnv* env) {
                |#define JENNY_CHECK_NULL(val)                      \
                |       do {                                        \
                |           if ((val) == nullptr) {                 \
                |""".trimMargin()
        )

        if (errorLoggerFunction.isNotEmpty()) {
            append(
                """
                |               ${errorLoggerFunction}(env, "can't init ${cppClassName}::" #val); \
                |""".trimMargin()
            )
        } else {
            append(
                """
                |               env->ExceptionDescribe();           \
                |""".trimMargin()
            )
        }

        append(
            """
                |               return false;                       \
                |           }                                       \
                |       } while(false)
                |
                |""".trimMargin()
        )
        append(
            """
                |    auto& state = getClassInitState();
                |""".trimMargin()
        )

        if (threadSafe) {
            append(
                """
                |   if (!state.sInited) {
                |       std::lock_guard<std::mutex> lg(state.sInitLock);
                |""".trimMargin()
            )
        }
        append(
            """
            |    if (!state.sInited) {
            |        auto clazz = env->FindClass(FULL_CLASS_NAME);
            |        JENNY_CHECK_NULL(clazz);
            |        state.sClazz = reinterpret_cast<jclass>(env->NewGlobalRef(clazz));
            |        env->DeleteLocalRef(clazz);
            |        JENNY_CHECK_NULL(state.sClazz);
            |
            |""".trimMargin()
        )
    }

    override fun generateSourcePostContent(
        cppClassName: String,
        endNamespace: String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String = buildString {
        val prefix = if (headerOnly) "/*static*/ inline" else "/*static*/"
        val lockGuard = if (threadSafe) {
            "std::lock_guard<std::mutex> lg(state.sInitLock);"
        } else {
            ""
        }
        append(
            """
            |        state.sInited = true;
            |    }
            |""".trimMargin()
        )
        if (threadSafe) {
            append("    }\n")
        }

        append(
            """
            |#undef JENNY_CHECK_NULL
            |   return true;
            |}
            |
            |${prefix} void ${cppClassName}::releaseClazz(JNIEnv* env) {
            |    auto& state = getClassInitState();
            |    if (state.sInited) {
            |        $lockGuard
            |        if (state.sInited) {
            |            env->DeleteGlobalRef(state.sClazz);
            |            state.sClazz = nullptr;
            |            state.sInited = false;
            |        }
            |    }
            |}
            |
            |""".trimMargin()
        )
        append("\n")
        append(endNamespace)
        append("\n")
    }

    override fun getConstructorIdInit(constructors: Map<JennyExecutableElement, Int>): String = buildString {
        constructors.forEach { (constructor, count) ->
            val name = "state.${JennyNameProvider.getConstructorName(count)}"
            val signature = Signature.getBinaryJennyElementSignature(constructor)

            append(
                """
                |        $name = env->GetMethodID(state.sClazz, "<init>", "$signature");
                |        JENNY_CHECK_NULL(${name});
                |
                |""".trimMargin()
            )
        }
        append('\n')
    }

    override fun getMethodIdInit(methods: Map<JennyExecutableElement, Int>): String = buildString {
        methods.forEach { (method, count) ->
            val name = "state.${JennyNameProvider.getElementName(method, count)}"
            val static = if (method.isStatic()) "Static" else ""
            val methodName = method.name
            val signature = Signature.getBinaryJennyElementSignature(method)
            append(
                """
                |        $name = env->Get${static}MethodID(state.sClazz, "$methodName", "$signature");
                |        JENNY_CHECK_NULL(${name});
                |
                |""".trimMargin()
            )
        }
        append('\n')
    }

    override fun getFieldIdInit(fields: Collection<JennyVarElement>): String = buildString {
        fields.forEachIndexed { index, field ->
            val name = "state.${JennyNameProvider.getElementName(field, index)}"
            val static = if (field.isStatic()) "Static" else ""
            val fieldName = field.name
            val signature = Signature.getBinaryJennyElementSignature(field)
            append(
                """
                |        $name = env->Get${static}FieldID(state.sClazz, "$fieldName", "$signature");
                |        JENNY_CHECK_NULL(${name});
                |
                |""".trimMargin()
            )
        }
        append('\n')
    }
}