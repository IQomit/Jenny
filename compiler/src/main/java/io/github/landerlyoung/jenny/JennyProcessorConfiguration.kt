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

package io.github.landerlyoung.jenny

import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration

data class JennyProcessorConfiguration(
    val templatesPath: String?,
    val outputDirectory: String,
    val glueNamespace: String = "",
    val proxyNamespace: String = "",
    val threadSafe: Boolean = true,
    val useJniHelper: Boolean = false,
    val headerOnlyProxy: Boolean = false,
    val allFields: Boolean = false,
    val allMethods: Boolean = false,
    val onlyPublicMethod: Boolean = true,
    val errorLoggingFunction: String = ""
) {
    fun provideProxyConfiguration(): JennyProxyConfiguration {
        return JennyProxyConfiguration(
            namespace = proxyNamespace,
            threadSafe = threadSafe,
            useJniHelper = useJniHelper,
            headerOnlyProxy = headerOnlyProxy,
            allFields = allFields,
            allMethods =allMethods ,
            onlyPublicMethod = onlyPublicMethod,
            errorLoggingFunction = errorLoggingFunction
        )
    }
}
