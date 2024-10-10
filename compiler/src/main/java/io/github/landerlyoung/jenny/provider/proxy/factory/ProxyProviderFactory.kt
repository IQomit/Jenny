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
     inline fun <reified T:Provider> createProvider(type: ProxyProviderType): T {
        return when(T::class){
            JennyProxyHeaderDefinitionsProvider::class ->ProxyHeaderProviderFactory.createProvider(type) as T
            JennyProxySourceDefinitionsProvider::class ->ProxySourceProviderFactory.createProvider(type) as T
            else -> throw Exception("${T::class.simpleName} not supported")
        }
    }
}

internal interface ProviderFactory <T:Provider> {
    fun createProvider(type: ProxyProviderType): T
}

internal object ProxyHeaderProviderFactory : ProviderFactory<JennyProxyHeaderDefinitionsProvider> {
    override fun createProvider(type: ProxyProviderType): JennyProxyHeaderDefinitionsProvider {
        return when (type) {
            ProxyProviderType.Default -> DefaultJennyProxyHeaderDefinitionsProvider()
            is ProxyProviderType.Template -> TemplateJennyProxyHeaderDefinitionsProvider(JteTemplate.createEngine(type.pathOfTemplate, type.pathOfTemplatesBuildFolder))
        }
    }
}

internal object ProxySourceProviderFactory : ProviderFactory<JennyProxySourceDefinitionsProvider> {
    override fun createProvider(type: ProxyProviderType): JennyProxySourceDefinitionsProvider {
        return when (type) {
            ProxyProviderType.Default -> DefaultJennyProxySourceDefinitionsProvider()
            is ProxyProviderType.Template -> TemplateJennyProxySourceDefinitionsProvider(JteTemplate.createEngine(type.pathOfTemplate, type.pathOfTemplatesBuildFolder))
        }
    }
}