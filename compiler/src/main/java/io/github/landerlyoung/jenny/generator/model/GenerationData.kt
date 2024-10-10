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

package io.github.landerlyoung.jenny.generator.model

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
    val constructors: List<JennyExecutableElement> = emptyList(),
    val methods: List<JennyExecutableElement> = emptyList(),
    val constants: List<JennyVarElement> = emptyList(),
    val fields: List<JennyVarElement> = emptyList(),
) {
    class Builder {
        private var classInfo: ClassInfo = ClassInfo()
        private var namespace: Namespace = Namespace()
        private var constructors: List<JennyExecutableElement> = emptyList()
        private var methods: List<JennyExecutableElement> = emptyList()
        private var constants: List<JennyVarElement> = emptyList()
        private var fields: List<JennyVarElement> = emptyList()
        private var defaultCppName: String = ""

        fun classInfo(classInfo: ClassInfo) = apply {
            this.classInfo = classInfo
        }

        fun constructors(constructors: List<JennyExecutableElement>) = apply {
            this.constructors = constructors
        }

        fun methods(methods: List<JennyExecutableElement>) = apply {
            this.methods = methods
        }

        fun constants(constants: List<JennyVarElement>) = apply {
            this.constants = constants
        }

        fun fields(fields: List<JennyVarElement>) = apply {
            this.fields = fields
        }

        fun namespace(namespace: Namespace) = apply {
            this.namespace = namespace
        }

        fun defaultCppName(defaultCppName: String) = apply {
            this.defaultCppName = defaultCppName
        }

        fun jennyClazz(clazz: JennyClazzElement) = apply {
            this.classInfo = extractClassInfo(clazz, defaultCppName)
            this.constructors = clazz.constructors.toList()
            this.methods = clazz.methods.toList()
            this.fields = clazz.fields.filter { !it.isConstant() }
            this.constants = clazz.fields.filter { it.isConstant() }
        }

        fun build() = HeaderData(
            classInfo = classInfo,
            namespace = namespace,
            constructors = constructors,
            methods = methods,
            constants = constants,
            fields = fields
        )

        private fun extractClassInfo(
            input: JennyClazzElement,
            defaultCppName: String = ""
        ): ClassInfo {
            val className = input.fullClassName
            val jniClassName = className.replace("_", "_1")
                .replace(".", "_")
                .stripNonASCII()

            return ClassInfo(
                simpleClassName = input.name,
                cppClassName = input.name + defaultCppName,
                className = className,
                slashClassName = className.replace('.', '/'),
                jniClassName = jniClassName
            )
        }
    }
}

internal data class SourceData(
    val headerFileName: String,
    val headerData: HeaderData
)