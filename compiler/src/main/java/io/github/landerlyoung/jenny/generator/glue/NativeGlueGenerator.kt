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

import io.github.landerlyoung.jenny.extractor.ConstantsExtractor
import io.github.landerlyoung.jenny.extractor.NativeMethodsExtractor
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.utils.CppFileNameGenerator
import io.github.landerlyoung.jenny.utils.FileHandler
import io.github.landerlyoung.jenny.utils.stripNonASCII
import kotlin.reflect.KClass

internal class NativeGlueGenerator : Generator<Any, Unit> {

    // Generators
    private val nativeGlueHeaderGenerator = NativeGlueHeaderGenerator()
    private val nativeSourceGenerator = NativeGlueSourceGenerator()

    // Extractors
    private val nativeMethodsExtractor = NativeMethodsExtractor()
    private val constantsExtractor = ConstantsExtractor()

    private val cppFileNameGenerator = CppFileNameGenerator()

    override fun generate(input: Any) {
        val classInfo = extractClassInfo(getClazz(input))
        val nativeMethods = nativeMethodsExtractor.extract(getClazz(input))
        val constants = constantsExtractor.extract(getClazz(input))

        val headerContent = nativeGlueHeaderGenerator.generate(HeaderData(classInfo, nativeMethods, constants))
        val headerFile = cppFileNameGenerator.generateHeaderFile(className = classInfo.simpleClassName)
        //TODO: Fix me parent is missing
        FileHandler.createOutputFile("", headerFile).use {
            it.write(headerContent.toByteArray(Charsets.UTF_8))
        }

        val sourceContent = nativeSourceGenerator.generate(SourceData(headerFile, classInfo, nativeMethods))
        val sourceFile = cppFileNameGenerator.generateSourceFile(className = classInfo.simpleClassName)
        //TODO: Fix me parent is missing
        FileHandler.createOutputFile("", sourceFile).use {
            it.write(sourceContent.toByteArray(Charsets.UTF_8))
        }
    }

    private fun getClazz(input: Any): KClass<*> {
        return when (input) {
            is KClass<*> -> input
            is Class<*> -> input.kotlin
            else -> throw IllegalArgumentException("Input must be a KClass or Class")
        }
    }

    private fun extractClassInfo(input: KClass<*>): ClassInfo {
        val className = input.qualifiedName ?: throw IllegalArgumentException("Class must have a qualified name")
        val simpleClassName = input.simpleName ?: throw IllegalArgumentException("Class must have a simple name")
        val slashClassName = className.replace('.', '/')
        val jniClassName = className.replace("_", "_1")
            .replace(".", "_")
            .stripNonASCII()
        return ClassInfo(
            simpleClassName = simpleClassName,
            className = className,
            slashClassName = slashClassName,
            jniClassName = jniClassName
        )
    }

}