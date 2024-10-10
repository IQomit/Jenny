/**
 * Copyright (C) 2024 The Qt Company Ltd.
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

package io.github.landerlyoung.jenny.generator.proxy

import io.github.landerlyoung.jenny.NativeFieldProxy
import io.github.landerlyoung.jenny.NativeMethodProxy
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.generator.model.HeaderData
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxyHeaderGenerator(
    private val headerProvider: JennyProxyHeaderDefinitionsProvider,
    private val sourceProvider: JennyProxySourceDefinitionsProvider,
    private var jennyProxyConfiguration: JennyProxyConfiguration
) : ProxyGenerator<HeaderData, String> {

    private val generateGetterForField: (JennyVarElement) -> Boolean = { field ->
        val annotation = field.getAnnotation(NativeFieldProxy::class.java)
        annotation?.getter ?: false
    }

    private val generateSetterForField: (JennyVarElement) -> Boolean = { field ->
        val annotation = field.getAnnotation(NativeFieldProxy::class.java)
        annotation?.setter ?: false
    }

    val generateForMethod: (JennyExecutableElement) -> Boolean = { method ->
        val annotation = method.getAnnotation(NativeMethodProxy::class.java)
        annotation?.enabled ?: jennyProxyConfiguration.allMethods
    }

    private val methodOverloadResolver = JennyMethodOverloadResolver()

    override fun generate(input: HeaderData): String {
        val classInfo = input.classInfo

        val constructors = input.constructors.visibility(jennyProxyConfiguration.onlyPublicMethod)
        val methods = input.methods.visibility(jennyProxyConfiguration.onlyPublicMethod)
        val fields = input.fields.visibility(jennyProxyConfiguration.onlyPublicMethod)

        val resolvedConstructors = methodOverloadResolver.resolve(constructors)
        val resolvedMethods = methodOverloadResolver.resolve(methods.filter { generateForMethod(it) })

        return buildString {
            append(headerProvider.autoGenerateNotice)
            append(
                headerProvider.getProxyHeaderInit(
                    jennyProxyConfiguration,
                    input.namespace.startOfNamespace,
                    classInfo
                )
            )
            append(headerProvider.getConstantsIdDeclare(input.constants))
            append(headerProvider.getProxyHeaderClazzInit())
            append(
                headerProvider.getConstructorsDefinitions(
                    classInfo.simpleClassName,
                    classInfo.cppClassName,
                    resolvedConstructors,
                    false
                )
            )
            append(
                headerProvider.getMethodsDefinitions(
                    resolvedMethods,
                    false
                )
            )
            append(
                headerProvider.getFieldsDefinitions(
                    fields = input.fields,
                    allMethods = methods,
                    useJniHelper = false,
                    getterSetterForAllFields = jennyProxyConfiguration.allFields,
                    generateGetterForField = generateGetterForField,
                    generateSetterForField = generateSetterForField,
                )
            )
            if (jennyProxyConfiguration.useJniHelper) {
                append(headerProvider.generateForJniHelper(classInfo.cppClassName))
                append(
                    headerProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        classInfo.cppClassName,
                        resolvedConstructors,
                        jennyProxyConfiguration.useJniHelper
                    )
                )
                append(
                    headerProvider.getMethodsDefinitions(
                        resolvedMethods,
                        true
                    )
                )
                append(
                    headerProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = methods,
                        useJniHelper = jennyProxyConfiguration.useJniHelper,
                        getterSetterForAllFields = jennyProxyConfiguration.allFields,
                        generateGetterForField = generateGetterForField,
                        generateSetterForField = generateSetterForField,
                    )
                )
            }

            append(headerProvider.initPreDefinition(jennyProxyConfiguration.threadSafe))

            append(headerProvider.getConstructorIdDeclare(resolvedConstructors))
            append(headerProvider.getMethodIdDeclare(resolvedMethods))
            append(headerProvider.getFieldIdDeclare(fields))
            append(headerProvider.initPostDefinition(input.namespace.endOfNameSpace))

            if (jennyProxyConfiguration.headerOnlyProxy) {
                append(
                    sourceProvider.generateSourcePreContent(
                        headerFileName = "",
                        startOfNamespace = input.namespace.startOfNamespace,
                        cppClassName = classInfo.cppClassName,
                        headerOnly = jennyProxyConfiguration.headerOnlyProxy,
                        errorLoggerFunction = jennyProxyConfiguration.errorLoggingFunction,
                        threadSafe = jennyProxyConfiguration.threadSafe,
                    )
                )
                append(sourceProvider.getConstructorIdInit(resolvedConstructors))
                append(sourceProvider.getMethodIdInit(resolvedMethods))
                append(sourceProvider.getFieldIdInit(fields))
                append(
                    sourceProvider.generateSourcePostContent(
                        cppClassName = classInfo.cppClassName,
                        endNamespace = input.namespace.endOfNameSpace,
                        headerOnly = jennyProxyConfiguration.headerOnlyProxy,
                        threadSafe = jennyProxyConfiguration.threadSafe,
                    )
                )
            }
        }
    }

    override fun applyConfiguration(configuration: JennyProxyConfiguration) {
        jennyProxyConfiguration = configuration
    }
}
