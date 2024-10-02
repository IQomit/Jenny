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
import io.github.landerlyoung.jenny.utils.CppFileNameGenerator
import io.github.landerlyoung.jenny.utils.isNative

internal class NativeGlueGenerator(private val outputDirectory: String) : Generator<JennyClazzElement, Unit> {

    // Generators
    private val nativeGlueHeaderGenerator = NativeGlueHeaderGenerator()
    private val nativeSourceGenerator = NativeGlueSourceGenerator()
    private val cppFileNameGenerator = CppFileNameGenerator()

    override fun generate(input: JennyClazzElement) {
        val headerData = HeaderData.Builder()
            .jennyClazz(input)
            .methods(input.methods.filter { it.isNative() })
            .build()
        val classInfo = headerData.classInfo
        // Header generation
        val headerContent = nativeGlueHeaderGenerator.generate(headerData)
        val headerFile = cppFileNameGenerator.generateHeaderFile(className = input.name)
        println("Header Content $headerContent")
//        FileHandler.createOutputFile(
//            outputDirectory,
//            Constants.JENNY_GEN_DIR_GLUE_HEADER + File.separatorChar + headerFile
//        ).use {
//            it.write(headerContent.toByteArray(Charsets.UTF_8))
//        }

        // Source generation
        val sourceContent = nativeSourceGenerator.generate(SourceData(headerFile, headerData))
        val sourceFile = cppFileNameGenerator.generateSourceFile(className = classInfo.simpleClassName)
//        FileHandler.createOutputFile(
//            outputDirectory,
//            Constants.JENNY_GEN_DIR_GLUE_SOURCE + File.separatorChar + sourceFile
//        ).use {
//            it.write(sourceContent.toByteArray(Charsets.UTF_8))
//        }
    }

}