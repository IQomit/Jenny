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

import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.generator.SourceData
import io.github.landerlyoung.jenny.utils.CppFileHelper
import io.github.landerlyoung.jenny.utils.FileHandler
import java.io.File

internal class NativeProxyGenerator(proxyConfiguration: ProxyConfiguration, private val outputDirectory: String) :
    Generator<JennyClazzElement, Unit> {

    private val nativeProxyHeaderGenerator = NativeProxyHeaderGenerator(proxyConfiguration)
    private val nativeProxySourceGenerator = NativeProxySourceGenerator(proxyConfiguration.threadSafe)
    private val cppFileHelper = CppFileHelper(proxyConfiguration.namespace)

    override fun generate(input: JennyClazzElement) {
        val headerData = HeaderData.Builder()
            .namespace(cppFileHelper.provideNamespace())
            .jennyClazz(input)
            .build()

        val headerContent = nativeProxyHeaderGenerator.generate(headerData)
        val headerFile = cppFileHelper.provideHeaderFile(className = input.name)
        saveContent(headerContent, headerFile)

        val sourceContent = nativeProxySourceGenerator.generate(SourceData(headerFile, headerData))
        val sourceFile = cppFileHelper.provideSourceFile(className = input.name)
        saveContent(sourceContent, sourceFile)
    }

    private fun saveContent(content: String, fileName: String) {
        FileHandler.createOutputFile(
            outputDirectory,
            JENNY_GEN_DIR_PROXY + File.separatorChar + fileName
        ).use {
            it.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    companion object {
        private const val JENNY_GEN_DIR_PROXY = "jenny.proxy"
    }

}