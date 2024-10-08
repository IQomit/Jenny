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

import io.github.landerlyoung.jenny.provider.Provider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.impl.DefaultJennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.impl.DefaultJennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.impl.TemplateJennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.impl.TemplateJennyProxySourceDefinitionsProvider

internal object ProxyProviderFactory {
    fun createProvider(isHeader: Boolean, type: ProxyProviderType): Provider {
        if(isHeader)
            return ProxyHeaderProviderFactory.createProvider(type)
        return ProxySourceProviderFactory.createProvider(type)
    }
}

internal interface ProviderFactory {
    fun createProvider(type: ProxyProviderType): Provider
}

internal object ProxyHeaderProviderFactory : ProviderFactory {
    override fun createProvider(type: ProxyProviderType): JennyProxyHeaderDefinitionsProvider {
        return when (type) {
            ProxyProviderType.Default -> DefaultJennyProxyHeaderDefinitionsProvider()
            is ProxyProviderType.Template -> TemplateJennyProxyHeaderDefinitionsProvider(
                JteTemplate.createEngine(
                    type.pathOfTemplate
                )
            )
        }
    }
}

internal object ProxySourceProviderFactory : ProviderFactory {
    override fun createProvider(type: ProxyProviderType): JennyProxySourceDefinitionsProvider {
        return when (type) {
            ProxyProviderType.Default -> DefaultJennyProxySourceDefinitionsProvider()
            is ProxyProviderType.Template -> TemplateJennyProxySourceDefinitionsProvider(
                JteTemplate.createEngine(
                    type.pathOfTemplate
                )
            )
        }
    }
}