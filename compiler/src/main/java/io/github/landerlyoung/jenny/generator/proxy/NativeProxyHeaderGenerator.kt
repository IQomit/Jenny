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
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxyHeaderGenerator(
    private val jennyProxyHeaderDefinitionsProvider: JennyProxyHeaderDefinitionsProvider,
    private val jennyProxySourceDefinitionsProvider: JennyProxySourceDefinitionsProvider,
    private var proxyConfiguration: ProxyConfiguration = ProxyConfiguration()
) : ProxyGenerator<HeaderData, String> {

    private val methodOverloadResolver = JennyMethodOverloadResolver()
    private val generateGetterForField: (JennyVarElement) -> Boolean = { field ->
        val annotation = field.getAnnotation(NativeFieldProxy::class.java)
        annotation?.getter ?: false
    }

    private val generateSetterForField: (JennyVarElement) -> Boolean = { field ->
        val annotation = field.getAnnotation(NativeFieldProxy::class.java)
        annotation?.setter ?: false
    }

    override fun generate(input: HeaderData): String {
        val classInfo = input.classInfo

        val constructors = input.constructors.visibility(proxyConfiguration.onlyPublicMethod)
        val methods = input.methods.visibility(proxyConfiguration.onlyPublicMethod)
        val fields = input.fields.visibility(proxyConfiguration.onlyPublicMethod)

        val resolvedConstructors = methodOverloadResolver.resolve(constructors)
        val resolvedMethods = methodOverloadResolver.resolve(methods)

        return buildString {
            append(jennyProxyHeaderDefinitionsProvider.autoGenerateNotice)
            append(
                jennyProxyHeaderDefinitionsProvider.getProxyHeaderInit(
                    proxyConfiguration,
                    input.namespace.startOfNamespace,
                    classInfo
                )
            )
            append(jennyProxyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
            append(jennyProxyHeaderDefinitionsProvider.getProxyHeaderClazzInit())
            append(
                jennyProxyHeaderDefinitionsProvider.getConstructorsDefinitions(
                    classInfo.simpleClassName,
                    resolvedConstructors,
                    false
                )
            )
            append(jennyProxyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, false))
            append(
                jennyProxyHeaderDefinitionsProvider.getFieldsDefinitions(
                    fields = input.fields,
                    allMethods = methods,
                    useJniHelper = false,
                    getterSetterForAllFields = proxyConfiguration.allFields,
                    generateGetterForField = generateGetterForField,
                    generateSetterForField = generateSetterForField,
                )
            )
            if (proxyConfiguration.useJniHelper) {
                append(jennyProxyHeaderDefinitionsProvider.generateForJniHelper(classInfo.simpleClassName))
                append(
                    jennyProxyHeaderDefinitionsProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        resolvedConstructors,
                        proxyConfiguration.useJniHelper
                    )
                )
                append(jennyProxyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, true))
                append(
                    jennyProxyHeaderDefinitionsProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = methods,
                        useJniHelper = proxyConfiguration.useJniHelper,
                        getterSetterForAllFields = proxyConfiguration.allFields,
                        generateGetterForField = generateGetterForField,
                        generateSetterForField = generateSetterForField,
                    )
                )
            }

            append(jennyProxyHeaderDefinitionsProvider.initPreDefinition(proxyConfiguration.threadSafe))

            append(jennyProxyHeaderDefinitionsProvider.getConstructorIdDeclare(resolvedConstructors))
            append(jennyProxyHeaderDefinitionsProvider.getMethodIdDeclare(resolvedMethods))
            append(jennyProxyHeaderDefinitionsProvider.getFieldIdDeclare(fields))
            append(jennyProxyHeaderDefinitionsProvider.initPostDefinition(input.namespace.endOfNameSpace))

            if (proxyConfiguration.headerOnlyProxy) {
                append(
                    jennyProxySourceDefinitionsProvider.generateSourcePreContent(
                        headerFileName = "",
                        startOfNamespace = input.namespace.startOfNamespace,
                        simpleClassName = classInfo.simpleClassName,
                        headerOnly = proxyConfiguration.headerOnlyProxy,
                        threadSafe = proxyConfiguration.threadSafe,
                    )
                )
                append(jennyProxySourceDefinitionsProvider.getConstructorIdInit(resolvedConstructors))
                append(jennyProxySourceDefinitionsProvider.getMethodIdInit(resolvedMethods))
                append(jennyProxySourceDefinitionsProvider.getFieldIdInit(fields))
                append(
                    jennyProxySourceDefinitionsProvider.generateSourcePostContent(
                        simpleClassName = classInfo.simpleClassName,
                        endNamespace = input.namespace.endOfNameSpace,
                        headerOnly = proxyConfiguration.headerOnlyProxy,
                        threadSafe = proxyConfiguration.threadSafe,
                    )
                )
            }
        }
    }

    override fun setConfiguration(configuration: ProxyConfiguration) {
        proxyConfiguration = configuration
    }
}
