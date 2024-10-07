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

import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.utils.isConstant
import io.github.landerlyoung.jenny.utils.stripNonASCII


internal data class Namespace(
    val startOfNamespace: String = "",
    val endOfNameSpace: String = "",
)

internal data class HeaderData(
    val classInfo: ClassInfo = ClassInfo(),
    val namespace: Namespace = Namespace(),
    val constructors: Collection<JennyExecutableElement> = emptyList(),
    val methods: Collection<JennyExecutableElement> = emptyList(),
    val constants: Collection<JennyVarElement> = emptyList(),
    val fields: Collection<JennyVarElement> = emptyList(),
) {
    class Builder {
        private var headerData = HeaderData()

        fun classInfo(classInfo: ClassInfo) = apply {
            headerData = headerData.copy(classInfo = classInfo)
        }

        fun constructors(constructors: Collection<JennyExecutableElement>) = apply {
            headerData = headerData.copy(constructors = constructors)
        }

        fun methods(methods: Collection<JennyExecutableElement>) = apply {
            headerData = headerData.copy(methods = methods)
        }

        fun constants(constants: Collection<JennyVarElement>) = apply {
            headerData = headerData.copy(constants = constants)
        }

        fun fields(fields: Collection<JennyVarElement>) = apply {
            headerData = headerData.copy(constants = fields)
        }

        fun namespace(namespace: Namespace) = apply {
            headerData = headerData.copy(namespace = namespace)
        }

        fun jennyClazz(clazz: JennyClazzElement) = apply {
            classInfo(extractClassInfo(clazz))
            constants(clazz.fields.filter { it.isConstant() })
            constructors(clazz.constructors)
            methods(clazz.methods)
            fields(clazz.fields)
        }

        fun build(): HeaderData {
            return headerData
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
}

internal data class SourceData(
    val headerFileName: String,
    val headerData: HeaderData
)