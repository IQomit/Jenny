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

package io.github.landerlyoung.jenny.generator

import io.github.landerlyoung.jenny.extractor.ConstantsExtractor
import io.github.landerlyoung.jenny.extractor.NativeMethodsExtractor
import io.github.landerlyoung.jenny.utils.stripNonASCII
import kotlin.reflect.KClass

internal class NativeGenerator : Generator<KClass<*>, Unit> {

    // Generators
    private val nativeHeaderGenerator = NativeHeaderGenerator()
    private val nativeSourceGenerator = NativeSourceGenerator()

    // Extractors
    private val nativeMethodsExtractor = NativeMethodsExtractor()
    private val constantsExtractor = ConstantsExtractor()

    override fun generate(input: KClass<*>) {
        val classInfo = extractClassInfo(input)
        val nativeMethods = nativeMethodsExtractor.extract(input)
        val constants = constantsExtractor.extract(input)
        // TODO: save the output content in a file (ioObject is going to be introduced to handle file creation/closing/saving)
        val headerContent = nativeHeaderGenerator.generate(HeaderData(classInfo, nativeMethods, constants))
        val sourceContent = nativeSourceGenerator.generate(classInfo to nativeMethods)
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