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

package io.github.landerlyoung.jenny.element.field

import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.type.JennyType
import java.lang.reflect.Field

internal class JennyFieldElement(private val reflectField: Field) : JennyVarElement {
    override val name: String
        get() = reflectField.name

    override val type: JennyType
        get() = reflectField.type

    override val annotations: List<String>
        get() = reflectField.annotations.map { it.annotationClass.simpleName ?: "Unknown" }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromReflectionModifiers(reflectField.modifiers)

    override val declaringClass: String?
        get() = reflectField.declaringClass.name

    override fun call(instance: Any?, vararg args: Any?): Any? {
        reflectField.isAccessible = true
        return reflectField.get(instance)
    }

    override fun describe(): String {
        return """
            Field Name: $name
            Type: $type
            Declaring Class: $declaringClass
            Modifiers: ${modifiers.joinToString(", ")}
            Annotations: ${annotations.joinToString(", ")}
        """.trimIndent()
    }
}