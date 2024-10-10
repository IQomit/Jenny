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
import io.github.landerlyoung.jenny.generator.jnihelper.JNIHelperGenerator
import io.github.landerlyoung.jenny.generator.model.HeaderData
import io.github.landerlyoung.jenny.generator.model.SourceData
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider
import io.github.landerlyoung.jenny.provider.proxy.factory.ProxyProviderFactory
import io.github.landerlyoung.jenny.provider.proxy.factory.ProxyProviderType
import io.github.landerlyoung.jenny.utils.CppClass
import io.github.landerlyoung.jenny.utils.CppFileHelper
import io.github.landerlyoung.jenny.utils.FileHandler
import java.io.File
import java.io.IOException

internal class NativeProxyGenerator(
    providerType: ProxyProviderType,
    private var proxyConfiguration: JennyProxyConfiguration,
    private val cppFileHelper: CppFileHelper,
    private var outputDirectory: String,
) : NativeProxy<JennyClazzElement, Unit> {

    private val headerProvider = ProxyProviderFactory.createProvider<JennyProxyHeaderDefinitionsProvider>(providerType)
    private val sourceProvider = ProxyProviderFactory.createProvider<JennyProxySourceDefinitionsProvider>(providerType)

    private val headerGenerator = NativeProxyHeaderGenerator(headerProvider, sourceProvider, proxyConfiguration)
    private val sourceGenerator = NativeProxySourceGenerator(sourceProvider, proxyConfiguration)

    private val jniHelperGenerator = JNIHelperGenerator(outputDirectory, cppFileHelper.jniHelperDefaultName)

    private val fusionProxyGenerator = FusionProxyGenerator(outputDirectory, proxyConfiguration.fusionProxyHeaderName)
    private var jniHelperGenerated = false

    override fun generate(input: JennyClazzElement) {
        val cppClass = generateHeaderFile(input)
        if (!proxyConfiguration.headerOnlyProxy) {
            generateSourceFile(input)
        }
        if (proxyConfiguration.useJniHelper && !jniHelperGenerated) {
            jniHelperGenerator.generate(Unit)
            jniHelperGenerated = true
        }
        fusionProxyGenerator.generate(cppClass)
    }

    private fun generateHeaderFile(input: JennyClazzElement): CppClass {
        val headerData = buildHeaderData(input)
        val headerContent = headerGenerator.generate(headerData)
        val headerFileName = cppFileHelper.provideHeaderFile(className = input.name)
        writeFileContent(headerContent, headerFileName)
        return CppClass(headerData.classInfo.cppClassName, cppFileHelper.namespaceNotation, headerFileName)
    }

    private fun generateSourceFile(input: JennyClazzElement) {
        val headerFileName = cppFileHelper.provideHeaderFile(className = input.name)
        val headerData = buildHeaderData(input)
        val sourceContent = sourceGenerator.generate(SourceData(headerFileName, headerData))
        val sourceFileName = cppFileHelper.provideSourceFile(className = input.name)
        writeFileContent(sourceContent, sourceFileName)
    }

    private fun buildHeaderData(input: JennyClazzElement): HeaderData {
        return HeaderData.Builder()
            .namespace(cppFileHelper.provideNamespace())
            .defaultCppName("Proxy")
            .jennyClazz(input)
            .build()
    }

    private fun writeFileContent(content: String, fileName: String) {
        try {
            FileHandler.createOutputStreamFrom(
                outputDirectory,
                JENNY_GEN_DIR_PROXY + File.separatorChar + fileName
            ).use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }
        } catch (e: IOException) {
            println("Error writing file $fileName: ${e.message}")
        }
    }

    override fun setOutputTargetPath(outputPath: String) {
        outputDirectory = outputPath
        jniHelperGenerator.setOutputTargetPath(outputPath)
        fusionProxyGenerator.setOutputTargetPath(outputPath)
    }

    override fun applyConfiguration(configuration: JennyProxyConfiguration) {
        proxyConfiguration = configuration

        cppFileHelper.setNamespace(configuration.namespace)
        headerGenerator.applyConfiguration(configuration)
        sourceGenerator.applyConfiguration(configuration)
        fusionProxyGenerator.setHeaderName(configuration.fusionProxyHeaderName)
    }

    companion object {
        private const val JENNY_GEN_DIR_PROXY = "jenny.proxy"
    }
}