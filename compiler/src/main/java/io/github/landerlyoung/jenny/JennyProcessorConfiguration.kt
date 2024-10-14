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
import io.github.landerlyoung.jenny.processor.ProviderConfiguration


/**
 * @param outputDirectory full path where the code is generated
 * @param glueNamespace name space for the C++ glue files (optional)
 * @param useTemplates flag to set the providers to use templates code
 * @param templateDirectory full path of custom templates
 * @param templateBuildDirectory full path of where the compiled templates is generated
 * @param threadSafe add mutex for C++ proxy files
 * @param useJniHelper flag to use JniHelper file
 * @param headerOnlyProxy only header file (.h) is generated
 * @param errorLoggerFunction Custom Error Logging function
 * @param fusionProxyHeaderName Custom Fusion header file name
 */

data class JennyProcessorConfiguration(
    val outputDirectory: String,
    val glueNamespace: String = "",
    val useTemplates: Boolean = true,
    val templateDirectory: String? = null,
    val templateBuildDirectory: String? = null,
    val threadSafe: Boolean = true,
    val useJniHelper: Boolean = false,
    val headerOnlyProxy: Boolean = false,
    val errorLoggerFunction: String = "",
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
            errorLoggingFunction = errorLoggerFunction,
            fusionProxyHeaderName = fusionProxyHeaderName
        )
    }

    fun provideTemplateConfiguration(): ProviderConfiguration {
        return ProviderConfiguration(useTemplates, templateDirectory, templateBuildDirectory)
    }

    companion object {
        private const val PREFIX = "jenny."
        // Constructing Keys

        /**
         * external error log function
         * void (function_type)(JNIEnv* env, const char* error);
         */
        private val ERROR_LOGGER_FUNCTION =
            PREFIX + JennyProcessorConfiguration::errorLoggerFunction.name

        private val OUTPUT_DIRECTORY = PREFIX + JennyProcessorConfiguration::outputDirectory.name

        private val FUSION_PROXY_HEADER_NAME =
            PREFIX + JennyProcessorConfiguration::fusionProxyHeaderName.name

        private val USE_TEMPLATES = PREFIX + JennyProcessorConfiguration::useTemplates.name
        private val TEMPLATE_DIRECTORY =
            PREFIX + JennyProcessorConfiguration::templateDirectory.name
        private val TEMPLATE_BUILD_DIRECTORY =
            PREFIX + JennyProcessorConfiguration::templateBuildDirectory.name

        private val HEADER_ONLY_PROXY = PREFIX + JennyProcessorConfiguration::headerOnlyProxy.name
        private val USE_JNI_HELPER = PREFIX + JennyProcessorConfiguration::useJniHelper.name
        private val THREAD_SAFE = PREFIX + JennyProcessorConfiguration::threadSafe.name

        val configurationOptions: Set<String>
            get() = setOf(
                THREAD_SAFE,
                ERROR_LOGGER_FUNCTION,
                USE_TEMPLATES,
                TEMPLATE_DIRECTORY,
                TEMPLATE_BUILD_DIRECTORY,
                OUTPUT_DIRECTORY,
                FUSION_PROXY_HEADER_NAME,
                HEADER_ONLY_PROXY,
                USE_JNI_HELPER,
            )

        fun fromOptions(options: Map<String, String>) = JennyProcessorConfiguration(
            outputDirectory = options[OUTPUT_DIRECTORY] ?: "src/main/cpp/gen",
            useTemplates = options[USE_TEMPLATES] == true.toString(),
            templateDirectory = options[TEMPLATE_DIRECTORY],
            templateBuildDirectory = options[TEMPLATE_BUILD_DIRECTORY],
            threadSafe = options[THREAD_SAFE] == true.toString(),
            useJniHelper = options[USE_JNI_HELPER] == true.toString(),
            headerOnlyProxy = options[HEADER_ONLY_PROXY] == true.toString(),
            errorLoggerFunction = options[ERROR_LOGGER_FUNCTION] ?: "",
            fusionProxyHeaderName = options[FUSION_PROXY_HEADER_NAME] ?: "JennyFusionProxy.h"
        )
    }
}
