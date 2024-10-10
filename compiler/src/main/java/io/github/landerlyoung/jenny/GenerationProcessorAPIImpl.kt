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
import io.github.landerlyoung.jenny.processor.NativeGlueProcessor
import io.github.landerlyoung.jenny.processor.NativeProxyProcessor

class GenerationProcessorAPIImpl(
    private var jennyProcessorConfiguration: JennyProcessorConfiguration,
) : GenerationProcessorAPI<Any> {

    private val nativeGlueProcessor = NativeGlueProcessor(jennyProcessorConfiguration.outputDirectory)

    private val nativeProxyProcessor = NativeProxyProcessor(
        outputDirectory = jennyProcessorConfiguration.outputDirectory,
        useTemplates = jennyProcessorConfiguration.useTemplates,
        proxyConfiguration = jennyProcessorConfiguration.provideProxyConfiguration()
    )

    override fun processForGlue(input: Any) = nativeGlueProcessor.process(input)

    override fun setGlueNamespace(namespace: String) = nativeGlueProcessor.setNamespace(namespace)

    override fun processForProxy(input: Any) = nativeProxyProcessor.process(input)

    override fun setProxyConfiguration(proxyConfiguration: JennyProxyConfiguration) =
        nativeProxyProcessor.applyConfiguration(proxyConfiguration)

    override fun setProcessorConfiguration(configuration: JennyProcessorConfiguration) {
        jennyProcessorConfiguration = configuration
        nativeProxyProcessor.apply {
            applyConfiguration(configuration.provideProxyConfiguration())
            setOutputTargetPath(configuration.outputDirectory)
        }
        nativeGlueProcessor.setOutputTargetPath(configuration.outputDirectory)
    }
}