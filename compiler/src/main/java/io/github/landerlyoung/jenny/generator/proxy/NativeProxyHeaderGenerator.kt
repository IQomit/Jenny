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
import io.github.landerlyoung.jenny.provider.JennyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.JennySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxyHeaderGenerator(
    private val proxyConfiguration: ProxyConfiguration,
    private val jennyHeaderDefinitionsProvider: JennyHeaderDefinitionsProvider,
    private val jennySourceDefinitionsProvider: JennySourceDefinitionsProvider
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
                jennyHeaderDefinitionsProvider.getProxyHeaderInit(
                    proxyConfiguration,
                    input.namespace.startOfNamespace,
                    classInfo
                )
            )
            append(jennyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
            append(jennyHeaderDefinitionsProvider.getProxyHeaderClazzInit())
            append(
                jennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                    classInfo.simpleClassName,
                    resolvedConstructors,
                    false
                )
            )
            append(jennyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, false))
            append(
                jennyHeaderDefinitionsProvider.getFieldsDefinitions(
                    fields = input.fields,
                    allMethods = methods,
                    useJniHelper = false,
                    getterSetterForAllFields = proxyConfiguration.allFields,
                    generateGetterForFields = proxyConfiguration.gettersForFields,
                    generateSetterForFields = proxyConfiguration.settersForFields,
                )
            )
            if (proxyConfiguration.useJniHelper) {
                append(jennyHeaderDefinitionsProvider.generateForJniHelper(classInfo.simpleClassName))
                append(
                    jennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        resolvedConstructors,
                        true
                    )
                )
                append(jennyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, true))
                append(
                    jennyHeaderDefinitionsProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = methods,
                        useJniHelper = true,
                        getterSetterForAllFields = proxyConfiguration.allFields,
                        generateGetterForFields = proxyConfiguration.gettersForFields,
                        generateSetterForFields = proxyConfiguration.settersForFields,
                    )
                )
            }

            append(jennyHeaderDefinitionsProvider.initPreDefinition(proxyConfiguration.threadSafe))

            append(jennyHeaderDefinitionsProvider.getConstructorIdDeclare(resolvedConstructors))
            append(jennyHeaderDefinitionsProvider.getMethodIdDeclare(resolvedMethods))
            append(jennyHeaderDefinitionsProvider.getFieldIdDeclare(fields))
            append(jennyHeaderDefinitionsProvider.initPostDefinition(input.namespace.endOfNameSpace))

            if (proxyConfiguration.headerOnlyProxy) {
                append(input.namespace.startOfNamespace)
                append("\n\n")
                append(
                    jennySourceDefinitionsProvider.generateSourcePreContent(
                        classInfo.simpleClassName,
                        headerOnly = true,
                        proxyConfiguration.threadSafe,
                    )
                )
                append(jennySourceDefinitionsProvider.getConstructorIdInit(resolvedConstructors))
                append(jennySourceDefinitionsProvider.getMethodIdInit(resolvedMethods))
                append(jennySourceDefinitionsProvider.getFieldIdInit(fields))
                append(
                    jennySourceDefinitionsProvider.generateSourcePostContent(
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
