/**
 * Copyright (C) 2024 The Qt Company Ltd.
 * Copyright 2016 landerlyoung@gmail.com
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
    val outputDirectory: String,
    val glueNamespace: String = "",
    val useTemplates: Boolean = true,
    val threadSafe: Boolean = true,
    val useJniHelper: Boolean = false,
    val headerOnlyProxy: Boolean = false,
    val errorLoggingFunction: String = "",
    val fusionProxyHeaderName: String = "JennyFusionProxy.h",
) {
    fun provideProxyConfiguration(): JennyProxyConfiguration {
        return JennyProxyConfiguration(
            namespace = "Jenny::Proxy",
            threadSafe = threadSafe,
            useJniHelper = useJniHelper,
            headerOnlyProxy = headerOnlyProxy,
            allFields = true,
            allMethods = true,
            onlyPublicMethod = true,
            errorLoggingFunction = errorLoggingFunction,
            fusionProxyHeaderName = fusionProxyHeaderName
        )
    }

    companion object {
        private const val PREFIX = "jenny."
        // Constructing Keys

        /**
         * external error log function
         * void (function_type)(JNIEnv* env, const char* error);
         */
        private val ERROR_LOGGER_FUNCTION = PREFIX + JennyProcessorConfiguration::errorLoggingFunction.name

        private val OUTPUT_DIRECTORY = PREFIX + JennyProcessorConfiguration::outputDirectory.name

        private val FUSION_PROXY_HEADER_NAME = PREFIX + JennyProcessorConfiguration::fusionProxyHeaderName.name

        private val USE_TEMPLATES = PREFIX + JennyProcessorConfiguration::useTemplates.name
        private val HEADER_ONLY_PROXY = PREFIX + JennyProcessorConfiguration::headerOnlyProxy.name
        private val USE_JNI_HELPER = PREFIX + JennyProcessorConfiguration::useJniHelper.name
        private val THREAD_SAFE = PREFIX + JennyProcessorConfiguration::threadSafe.name

        val configurationOptions: Set<String>
            get() = setOf(
                THREAD_SAFE,
                ERROR_LOGGER_FUNCTION,
                USE_TEMPLATES,
                OUTPUT_DIRECTORY,
                FUSION_PROXY_HEADER_NAME,
                HEADER_ONLY_PROXY,
                USE_JNI_HELPER,
            )

        fun fromOptions(options: Map<String, String>) = JennyProcessorConfiguration(
            outputDirectory = options[OUTPUT_DIRECTORY] ?: "src/main/cpp/gen",
            useTemplates = options[USE_TEMPLATES] == true.toString(),
            threadSafe = options[THREAD_SAFE] == true.toString(),
            useJniHelper = options[USE_JNI_HELPER] == true.toString(),
            headerOnlyProxy = options[HEADER_ONLY_PROXY] == true.toString(),
            errorLoggingFunction = options[ERROR_LOGGER_FUNCTION] ?: "",
            fusionProxyHeaderName = options[FUSION_PROXY_HEADER_NAME] ?: "JennyFusionProxy.h"
        )
    }
}
