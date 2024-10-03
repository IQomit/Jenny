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
import io.github.landerlyoung.jenny.generator.SourceData
import io.github.landerlyoung.jenny.provider.JennySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxySourceGenerator(
    private val jennySourceDefinitionsProvider: JennySourceDefinitionsProvider,
    private val threadSafe: Boolean,
    private val onlyPublicMethod: Boolean
) : Generator<SourceData, String> {
    private val methodOverloadResolver = JennyMethodOverloadResolver()

    override fun generate(input: SourceData): String {
        val header = input.headerData
        val constructors = header.constructors.visibility(onlyPublicMethod)
        val methods = header.methods.visibility(onlyPublicMethod)
        val resolvedConstructors = methodOverloadResolver.resolve(constructors)
        val resolvedMethods = methodOverloadResolver.resolve(methods)

        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)

            append(
                """
                |#include "${input.headerFileName}"
                |
                |""".trimMargin()
            )
            append(input.headerData.namespace.startOfNamespace)
            append("\n\n")
            append(
                jennySourceDefinitionsProvider.generateSourcePreContent(
                    header.classInfo.simpleClassName,
                    headerOnly = false,
                    threadSafe,
                )
            )
            append(jennySourceDefinitionsProvider.getConstructorIdInit(resolvedConstructors))
            append(jennySourceDefinitionsProvider.getMethodIdInit(resolvedMethods))
            append(jennySourceDefinitionsProvider.getFieldIdInit(header.fields))
            append(
                jennySourceDefinitionsProvider.generateSourcePostContent(
                    simpleClassName = header.classInfo.simpleClassName,
                    endNamespace = input.headerData.namespace.endOfNameSpace,
                    headerOnly = true,
                    threadSafe,
                )
            )
        }
    }
}