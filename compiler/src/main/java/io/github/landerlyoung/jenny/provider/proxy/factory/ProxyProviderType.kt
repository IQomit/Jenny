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

package io.github.landerlyoung.jenny.provider.proxy.factory

import io.github.landerlyoung.jenny.processor.ProviderConfiguration

internal sealed interface ProxyProviderType {
    data object Default : ProxyProviderType
    data class Template(
        val pathOfTemplate: String = System.getProperty("user.dir") + "/compiler/src/main/resources/jte",
        val pathOfTemplatesBuildFolder: String? = null
    ) : ProxyProviderType
    data class QTemplate(
        val pathOfTemplate: String = System.getProperty("user.dir") + "/compiler/src/main/resources/jte",
        val pathOfTemplatesBuildFolder: String? = null
    ) : ProxyProviderType
}

internal object ProxyProviderTypeFactory {
    fun createProviderType(configuration: ProviderConfiguration): ProxyProviderType {
        if(configuration.useQjniTemplates)
            return ProxyProviderType.QTemplate(
                pathOfTemplate = configuration.templateDirectory
                    ?: (System.getProperty("user.dir") + "/compiler/src/main/resources/jte"),
                pathOfTemplatesBuildFolder = configuration.templateBuildDirectory
            )
        return if (configuration.useTemplates) {
            ProxyProviderType.Template(
                pathOfTemplate = configuration.templateDirectory
                    ?: (System.getProperty("user.dir") + "/compiler/src/main/resources/jte"),
                pathOfTemplatesBuildFolder = configuration.templateBuildDirectory
            )
        } else {
            ProxyProviderType.Default
        }
    }
}