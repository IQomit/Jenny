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

package io.github.landerlyoung.jenny.generator.glue

import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.generator.SourceData
import io.github.landerlyoung.jenny.utils.CppFileHelper
import io.github.landerlyoung.jenny.utils.FileHandler
import io.github.landerlyoung.jenny.utils.isNative
import java.io.File

internal class NativeGlueGenerator(namespace: String, private val outputDirectory: String) :
    Generator<JennyClazzElement, Unit> {

    // Generators
    private val nativeGlueHeaderGenerator = NativeGlueHeaderGenerator()
    private val nativeSourceGenerator = NativeGlueSourceGenerator()
    private val cppFileHelper = CppFileHelper(namespace)

    override fun generate(input: JennyClazzElement) {
        val headerData = HeaderData.Builder()
            .jennyClazz(input)
            .namespace(cppFileHelper.provideNamespace())
            .methods(input.methods.filter { it.isNative() })
            .build()


        // Header generation
        val headerContent = nativeGlueHeaderGenerator.generate(headerData)
        val headerFile = cppFileHelper.provideHeaderFile(className = input.name)
        saveContent(headerContent, headerFile)

        // Source generation
        val sourceContent = nativeSourceGenerator.generate(SourceData(headerFile, headerData))
        val sourceFile = cppFileHelper.provideSourceFile(className = input.name)
        saveContent(sourceContent, sourceFile)
    }

    private fun saveContent(content: String, fileName: String) {
        FileHandler.createOutputFile(
            outputDirectory,
            JENNY_GEN_DIR_GLUE + File.separatorChar + fileName
        ).use {
            it.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    companion object {
        private const val JENNY_GEN_DIR_GLUE = "jenny.glue"
    }

}