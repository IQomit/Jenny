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

internal class NativeProxyHeaderGenerator(
    private val proxyConfiguration: ProxyConfiguration
) : Generator<HeaderData, String> {

    private val methodOverloadResolver = JennyMethodOverloadResolver()
    private val getterSetterForAllFields = true

    override fun generate(input: HeaderData): String {
        val classInfo = input.classInfo
        val constructors = methodOverloadResolver.resolve(input.constructors)
        val methods = methodOverloadResolver.resolve(input.methods)
        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)
            append(JennyHeaderDefinitionsProvider.getProxyHeaderInit(proxyConfiguration, classInfo))
            append(JennyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
            append(JennyHeaderDefinitionsProvider.getProxyHeaderClazzInit())
            append(
                JennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                    classInfo.simpleClassName, constructors, false
                )
            )
            append(
                JennyHeaderDefinitionsProvider.getMethodsDefinitions(
                    methodOverloadResolver.resolve(input.methods), false
                )
            )
            append(
                JennyHeaderDefinitionsProvider.getFieldsDefinitions(
                    fields = input.fields,
                    allMethods = input.methods,
                    useJniHelper = false,
                    getterSetterForAllFields = getterSetterForAllFields,
                )
            )
            if (proxyConfiguration.useJniHelper) {
                append(JennyHeaderDefinitionsProvider.generateForJniHelper(classInfo.simpleClassName))
                append(
                    JennyHeaderDefinitionsProvider.getConstructorsDefinitions(
                        classInfo.simpleClassName,
                        constructors,
                        true
                    )
                )
                append(JennyHeaderDefinitionsProvider.getMethodsDefinitions(methods, true))
                append(
                    JennyHeaderDefinitionsProvider.getFieldsDefinitions(
                        fields = input.fields,
                        allMethods = input.methods,
                        useJniHelper = true,
                        getterSetterForAllFields = getterSetterForAllFields,
                    )
                )
            }
            append(JennyHeaderDefinitionsProvider.initPreDefinition(proxyConfiguration.threadSafe))

            append(JennyHeaderDefinitionsProvider.getConstructorIdDeclare(input.constructors))
            append(JennyHeaderDefinitionsProvider.getMethodIdDeclare(input.methods))
            append(JennyHeaderDefinitionsProvider.getFieldIdDeclare(input.fields))
            append(JennyHeaderDefinitionsProvider.initPostDefinition())

            if (proxyConfiguration.headerOnlyProxy) {
                append("\n\n")
                append(
                    JennyHeaderDefinitionsProvider.generateSourceContent(
                        classInfo.simpleClassName,
                        proxyConfiguration.threadSafe
                    )
                )
            }
        }
    }
}