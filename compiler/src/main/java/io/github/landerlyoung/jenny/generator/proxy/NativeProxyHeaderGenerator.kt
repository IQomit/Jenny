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

import io.github.landerlyoung.jenny.Constants
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxyHeaderGenerator(
    private val proxyConfiguration: ProxyConfiguration,
    private val jennyProxyHeaderDefinitionsProvider: JennyProxyHeaderDefinitionsProvider,
    private val jennyProxySourceDefinitionsProvider: JennyProxySourceDefinitionsProvider
) : Generator<HeaderData, String> {

    private val methodOverloadResolver = JennyMethodOverloadResolver()

    override fun generate(input: HeaderData): String {
        val classInfo = input.classInfo

        val constructors = input.constructors.visibility(proxyConfiguration.onlyPublicMethod)
        val methods = input.methods.visibility(proxyConfiguration.onlyPublicMethod)
        val fields = input.fields.visibility(proxyConfiguration.onlyPublicMethod)

        val resolvedConstructors = methodOverloadResolver.resolve(constructors)
        val resolvedMethods = methodOverloadResolver.resolve(methods)

        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)
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
                    generateGetterForFields = proxyConfiguration.gettersForFields,
                    generateSetterForFields = proxyConfiguration.settersForFields,
                )
            )
            if (proxyConfiguration.useJniHelper) {
                append(jennyProxyHeaderDefinitionsProvider.generateForJniHelper(classInfo.simpleClassName))
                append(
                    jennyProxyHeaderDefinitionsProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        resolvedConstructors,
                        true
                    )
                )
                append(jennyProxyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, true))
                append(
                    jennyProxyHeaderDefinitionsProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = methods,
                        useJniHelper = true,
                        getterSetterForAllFields = proxyConfiguration.allFields,
                        generateGetterForFields = proxyConfiguration.gettersForFields,
                        generateSetterForFields = proxyConfiguration.settersForFields,
                    )
                )
            }

            append(jennyProxyHeaderDefinitionsProvider.initPreDefinition(proxyConfiguration.threadSafe))

            append(jennyProxyHeaderDefinitionsProvider.getConstructorIdDeclare(resolvedConstructors))
            append(jennyProxyHeaderDefinitionsProvider.getMethodIdDeclare(resolvedMethods))
            append(jennyProxyHeaderDefinitionsProvider.getFieldIdDeclare(fields))
            append(jennyProxyHeaderDefinitionsProvider.initPostDefinition(input.namespace.endOfNameSpace))

            if (proxyConfiguration.headerOnlyProxy) {
                append(input.namespace.startOfNamespace)
                append("\n\n")
                append(
                    jennyProxySourceDefinitionsProvider.generateSourcePreContent(
                        classInfo.simpleClassName,
                        headerOnly = true,
                        proxyConfiguration.threadSafe,
                    )
                )
                append(jennyProxySourceDefinitionsProvider.getConstructorIdInit(resolvedConstructors))
                append(jennyProxySourceDefinitionsProvider.getMethodIdInit(resolvedMethods))
                append(jennyProxySourceDefinitionsProvider.getFieldIdInit(fields))
                append(
                    jennyProxySourceDefinitionsProvider.generateSourcePostContent(
                        classInfo.simpleClassName,
                        endNamespace = input.namespace.endOfNameSpace,
                        headerOnly = true,
                        proxyConfiguration.threadSafe,
                    )
                )
            }
        }
    }
}
