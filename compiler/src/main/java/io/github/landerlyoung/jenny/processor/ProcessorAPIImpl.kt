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

package io.github.landerlyoung.jenny.processor

import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration
import io.github.landerlyoung.jenny.processor.jenny.NativeGlueProcessor
import io.github.landerlyoung.jenny.processor.jenny.NativeProxyProcessor

class ProcessorAPIImpl(outputDirectory: String, templatesPath: String? = null) : ProcessorAPI<Any> {
    private var configuration = JennyProcessorConfiguration(
        outputDirectory = outputDirectory,
        templatesPath = templatesPath
    )

    private val nativeGlueProcessor =
        NativeGlueProcessor(configuration.outputDirectory)
            .apply {
                this.setNamespace(configuration.glueNamespace)
            }
    private val nativeProxyProcessor =
        NativeProxyProcessor(
            outputDirectory = configuration.outputDirectory,
            templatesPath = configuration.templatesPath
        ).apply {
            this.applyConfiguration(configuration.provideProxyConfiguration())
        }

    override fun processForGlue(input: Any) {
        nativeGlueProcessor.process(input)
    }

    override fun setGlueNamespace(namespace: String) {
        configuration = configuration.copy(glueNamespace = namespace)
        nativeGlueProcessor.setNamespace(namespace)
    }

    override fun processForProxy(input: Any) {
        nativeProxyProcessor.process(input)
    }

    override fun setProxyConfiguration(configuration: JennyProxyConfiguration) {
        this.configuration = this.configuration.copy(
            proxyNamespace = configuration.namespace,
            threadSafe = configuration.threadSafe,
            useJniHelper = configuration.useJniHelper,
            headerOnlyProxy = configuration.headerOnlyProxy,
            allFields = configuration.allFields,
            allMethods = configuration.allMethods,
            onlyPublicMethod = configuration.onlyPublicMethod,
            errorLoggingFunction = configuration.errorLoggingFunction
        )
        nativeProxyProcessor.applyConfiguration(configuration)
    }

    override fun setProcessorConfiguration(configuration: JennyProcessorConfiguration) {
        this.configuration = configuration
        nativeProxyProcessor.applyConfiguration(configuration.provideProxyConfiguration())

        nativeProxyProcessor.setOutputTargetPath(configuration.outputDirectory)
        nativeGlueProcessor.setOutputTargetPath(configuration.outputDirectory)
    }
}