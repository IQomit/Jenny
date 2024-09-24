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
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.utils.CppFileNameGenerator
import io.github.landerlyoung.jenny.utils.stripNonASCII

internal class NativeGlueGenerator : Generator<JennyClazzElement, Unit> {

    // Generators
    private val nativeGlueHeaderGenerator = NativeGlueHeaderGenerator()
    private val nativeSourceGenerator = NativeGlueSourceGenerator()
    private val cppFileNameGenerator = CppFileNameGenerator()

    override fun generate(input: JennyClazzElement) {
        val classInfo = extractClassInfo(input)

        val nativeMethods = extractNativeMethods(input.methods)
        val constants = extractConstants(input.fields)

        val headerContent = nativeGlueHeaderGenerator.generate(HeaderData(classInfo, nativeMethods, constants))
        val headerFile = cppFileNameGenerator.generateHeaderFile(className = classInfo.simpleClassName)

        val sourceContent = nativeSourceGenerator.generate(SourceData(headerFile, classInfo, nativeMethods))
        val sourceFile = cppFileNameGenerator.generateSourceFile(className = classInfo.simpleClassName)
    }

    private fun extractConstants(fields: List<JennyVarElement>): List<JennyVarElement> {
        return fields.filter { field ->
            JennyModifier.STATIC in field.modifiers && JennyModifier.FINAL in field.modifiers
        }
    }

    private fun extractNativeMethods(methods: List<JennyExecutableElement>): List<JennyExecutableElement> {
        return methods.filter { method ->
            JennyModifier.NATIVE in method.modifiers
        }
    }

    private fun extractClassInfo(input: JennyClazzElement): ClassInfo {
        val className = input.fullClassName
        val simpleClassName = input.name
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