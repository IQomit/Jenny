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
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.utils.isConstant
import io.github.landerlyoung.jenny.utils.stripNonASCII

internal class NativeProxyGenerator(proxyConfiguration: ProxyConfiguration) : Generator<JennyClazzElement, Unit> {

    private val nativeProxyHeaderGenerator = NativeProxyHeaderGenerator(proxyConfiguration)

    override fun generate(input: JennyClazzElement) {
        val classInfo = extractClassInfo(input)

        val headerData = HeaderData(
            classInfo = classInfo,
            constructors = input.constructors,
            methods = input.methods,
            constants = extractConstants(input.fields),
            fields = input.fields
        )
        val headerContent = nativeProxyHeaderGenerator.generate(headerData)
        println("Proxy Header Content:::::::: $headerContent")
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

    private fun extractConstants(fields: List<JennyVarElement>): List<JennyVarElement> {
        return fields.filter { field ->
            field.isConstant()
        }
    }
}