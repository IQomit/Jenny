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
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.JennyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.utils.JennySourceDefinitionsProvider
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxyHeaderGenerator(
    private val proxyConfiguration: ProxyConfiguration
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
                JennyHeaderDefinitionsProvider.getProxyHeaderInit(
                    proxyConfiguration,
                    input.namespace.startOfNamespace,
                    classInfo
                )
            )
            append(JennyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
            append(JennyHeaderDefinitionsProvider.getProxyHeaderClazzInit())
            append(
                JennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                    classInfo.simpleClassName,
                    resolvedConstructors,
                    false
                )
            )
            append(JennyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, false))
            append(
                JennyHeaderDefinitionsProvider.getFieldsDefinitions(
                    fields = input.fields,
                    allMethods = methods,
                    useJniHelper = false,
                    getterSetterForAllFields = proxyConfiguration.allFields,
                    generateGetterForFields = proxyConfiguration.gettersForFields,
                    generateSetterForFields = proxyConfiguration.settersForFields,
                )
            )
            if (proxyConfiguration.useJniHelper) {
                append(JennyHeaderDefinitionsProvider.generateForJniHelper(classInfo.simpleClassName))
                append(
                    JennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        resolvedConstructors,
                        true
                    )
                )
                append(JennyHeaderDefinitionsProvider.getMethodsDefinitions(resolvedMethods, true))
                append(
                    JennyHeaderDefinitionsProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = methods,
                        useJniHelper = true,
                        getterSetterForAllFields = proxyConfiguration.allFields,
                        generateGetterForFields = proxyConfiguration.gettersForFields,
                        generateSetterForFields = proxyConfiguration.settersForFields,
                    )
                )
            }

            append(JennyHeaderDefinitionsProvider.initPreDefinition(proxyConfiguration.threadSafe))

            append(JennyHeaderDefinitionsProvider.getConstructorIdDeclare(resolvedConstructors))
            append(JennyHeaderDefinitionsProvider.getMethodIdDeclare(resolvedMethods))
            append(JennyHeaderDefinitionsProvider.getFieldIdDeclare(fields))
            append(JennyHeaderDefinitionsProvider.initPostDefinition(input.namespace.endOfNameSpace))

            if (proxyConfiguration.headerOnlyProxy) {
                append("\n\n")
                append(
                    JennySourceDefinitionsProvider.generateSourcePreContent(
                        classInfo.simpleClassName,
                        headerOnly = true,
                        proxyConfiguration.threadSafe,
                    )
                )
                append(JennySourceDefinitionsProvider.getConstructorIdInit(resolvedConstructors))
                append(JennySourceDefinitionsProvider.getMethodIdInit(resolvedMethods))
                append(JennySourceDefinitionsProvider.getFieldIdInit(fields))
                append(
                    JennySourceDefinitionsProvider.generateSourcePostContent(
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
