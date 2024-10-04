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
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.generator.SourceData
import io.github.landerlyoung.jenny.provider.proxy.TemplateJennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.TemplateJennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.utils.CppFileHelper
import io.github.landerlyoung.jenny.utils.FileHandler
import io.github.landerlyoung.jenny.utils.JteTemplate
import java.io.File
import java.io.IOException

internal class NativeProxyGenerator(
    private val cppFileHelper: CppFileHelper,
    private val outputDirectory: String
) : ProxyGenerator<JennyClazzElement, Unit> {
    private val templatesPath = System.getProperty("user.dir") + "/compiler/src/main/resources/jte"
    private val jennyHeaderDefinitionsProvider = TemplateJennyProxyHeaderDefinitionsProvider(JteTemplate.createEngine(templatesPath))
    private val jennySourceDefinitionsProvider = TemplateJennyProxySourceDefinitionsProvider(JteTemplate.createEngine(templatesPath))
//    private val jennySourceDefinitionsProvider = DefaultJennyProxyProxySourceDefinitionsProvider()

    private val nativeProxyHeaderGenerator =
        NativeProxyHeaderGenerator(
            jennyProxyHeaderDefinitionsProvider = jennyHeaderDefinitionsProvider,
            jennyProxySourceDefinitionsProvider = jennySourceDefinitionsProvider
        )
    private val nativeProxySourceGenerator = NativeProxySourceGenerator(jennySourceDefinitionsProvider)

    private var headerOnlyProxy = false
    override fun generate(input: JennyClazzElement) {
        generateHeaderFile(input)
        if (!headerOnlyProxy) {
            generateSourceFile(input)
        }
    }

    private fun generateHeaderFile(input: JennyClazzElement) {
        val headerData = createHeaderData(input)
        val headerContent = nativeProxyHeaderGenerator.generate(headerData)
        val headerFileName = cppFileHelper.provideHeaderFile(className = input.name)
        writeFileContent(headerContent, headerFileName)
    }

    private fun generateSourceFile(input: JennyClazzElement) {
        val headerFileName = cppFileHelper.provideHeaderFile(className = input.name)
        val headerData = createHeaderData(input)
        val sourceContent = nativeProxySourceGenerator.generate(SourceData(headerFileName, headerData))
        val sourceFileName = cppFileHelper.provideSourceFile(className = input.name)
        writeFileContent(sourceContent, sourceFileName)
    }

    private fun createHeaderData(input: JennyClazzElement): HeaderData {
        return HeaderData.Builder()
            .namespace(cppFileHelper.provideNamespace())
            .jennyClazz(input)
            .build()
    }

    private fun writeFileContent(content: String, fileName: String) {
        try {
            FileHandler.createOutputFile(
                outputDirectory,
                JENNY_GEN_DIR_PROXY + File.separatorChar + fileName
            ).use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }
        } catch (e: IOException) {
            println("Error writing file $fileName: ${e.message}")
        }
    }

    override fun setConfiguration(configuration: ProxyConfiguration) {
        headerOnlyProxy = configuration.headerOnlyProxy
        nativeProxyHeaderGenerator.setConfiguration(configuration)
        nativeProxySourceGenerator.setConfiguration(configuration)
    }

    companion object {
        private const val JENNY_GEN_DIR_PROXY = "jenny.proxy"
    }
}