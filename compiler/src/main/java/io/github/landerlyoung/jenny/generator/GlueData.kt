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

internal data class HeaderData(
    val classInfo: ClassInfo,
    val constructors: Collection<JennyExecutableElement>,
    val methods: Collection<JennyExecutableElement>,
    val constants: Collection<JennyVarElement>,
    val fields: Collection<JennyVarElement>,
) {
    class Builder {
        private var classInfo: ClassInfo? = null
        private var constructors: Collection<JennyExecutableElement> = emptyList()
        private var methods: Collection<JennyExecutableElement> = emptyList()
        private var constants: Collection<JennyVarElement> = emptyList()
        private var fields: Collection<JennyVarElement> = emptyList()

        fun classInfo(classInfo: ClassInfo) = apply {
            this.classInfo = classInfo
        }

        fun constructors(constructors: Collection<JennyExecutableElement>) = apply {
            this.constructors = constructors
        }

        fun methods(methods: Collection<JennyExecutableElement>) = apply {
            this.methods = methods
        }

        fun constants(constants: Collection<JennyVarElement>) = apply {
            this.constants = constants
        }

        fun fields(fields: Collection<JennyVarElement>) = apply {
            this.fields = fields
        }

        fun jennyClazz(clazz: JennyClazzElement) = apply {
            this.classInfo = extractClassInfo(clazz)
            this.constants = clazz.fields.filter { it.isConstant() }
            this.constructors = clazz.constructors
            this.methods = clazz.methods
            this.fields = clazz.fields
        }

        fun build(): HeaderData {
            val classInfo = this.classInfo
                ?: throw IllegalStateException("ClassInfo must be provided")
            return HeaderData(
                classInfo = classInfo,
                constructors = constructors,
                methods = methods,
                constants = constants,
                fields = fields
            )
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