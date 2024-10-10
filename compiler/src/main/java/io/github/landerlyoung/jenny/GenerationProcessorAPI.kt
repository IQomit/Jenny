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

interface GenerationProcessorAPI<T> {
    /**
     * Processes the given input for Native glue files generation.
     * @param input the input object to be processed as glue file.
     */
    fun processForGlue(input: T)

    /**
     * Sets the namespace for the Glue generation.
     * @param namespace the namespace to be used for Glue.
     */
    fun setGlueNamespace(namespace: String)

    /**
     * Processes the given input for proxy files generation.
     * @param input the input object to be processed as proxy file.
     */
    fun processForProxy(input: T)

    /**
     * Applies configuration settings for the processor.
     * @param proxyConfiguration the configuration object for the proxy processor.
     */
    fun setProxyConfiguration(proxyConfiguration: JennyProxyConfiguration)


    /**
     * Applies configuration settings for the processor.
     * @param configuration the configuration object for the proxy processor.
     */
    fun setProcessorConfiguration(configuration: JennyProcessorConfiguration)
}