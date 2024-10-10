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

import io.github.landerlyoung.jenny.generator.model.SourceData
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.resolver.JennyMethodOverloadResolver
import io.github.landerlyoung.jenny.utils.visibility

internal class NativeProxySourceGenerator(
    private val sourceProvider: JennyProxySourceDefinitionsProvider,
    private var jennyProxyConfiguration: JennyProxyConfiguration
) : ProxyGenerator<SourceData, String> {

    private val methodOverloadResolver = JennyMethodOverloadResolver()

    override fun generate(input: SourceData): String {
        val header = input.headerData
        val constructors = header.constructors.visibility(jennyProxyConfiguration.onlyPublicMethod)
        val methods = header.methods.visibility(jennyProxyConfiguration.onlyPublicMethod)
        val resolvedConstructors = methodOverloadResolver.resolve(constructors)
        val resolvedMethods = methodOverloadResolver.resolve(methods)

        return buildString {
            append(sourceProvider.autoGenerateNotice)
            append(
                sourceProvider.generateSourcePreContent(
                    headerFileName = input.headerFileName,
                    startOfNamespace = input.headerData.namespace.startOfNamespace,
                    cppClassName = header.classInfo.cppClassName,
                    headerOnly = jennyProxyConfiguration.headerOnlyProxy,
                    errorLoggerFunction = jennyProxyConfiguration.errorLoggingFunction,
                    threadSafe = jennyProxyConfiguration.threadSafe,
                )
            )
            append(sourceProvider.getConstructorIdInit(resolvedConstructors))
            append(sourceProvider.getMethodIdInit(resolvedMethods))
            append(sourceProvider.getFieldIdInit(header.fields))
            append(
                sourceProvider.generateSourcePostContent(
                    cppClassName = header.classInfo.cppClassName,
                    endNamespace = input.headerData.namespace.endOfNameSpace,
                    headerOnly = jennyProxyConfiguration.headerOnlyProxy,
                    threadSafe = jennyProxyConfiguration.threadSafe,
                )
            )
        }
    }

    override fun applyConfiguration(configuration: JennyProxyConfiguration) {
        jennyProxyConfiguration = configuration
    }
}