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

package io.github.landerlyoung.jenny.element.method

import io.github.landerlyoung.jenny.element.JennyCallableElement
import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.model.JennyParameter
import io.github.landerlyoung.jenny.element.model.type.JennyType

internal interface JennyExecutableElement : JennyElement, JennyCallableElement {
    fun isConstructor(): Boolean

    val parameters: List<JennyParameter>
    val exceptionsTypes: List<String>
    val returnType: JennyType

    companion object {
        fun createWithNewName(original: JennyExecutableElement, newName: String): JennyExecutableElement {
            return original.withNewName(newName) // This calls the internal method
        }
    }

    fun withNewName(newName: String): JennyExecutableElement

    override fun describe(): String {
        return if (isConstructor()) {
            """
                Constructor for: $${declaringClass?.name}
                Modifiers: ${modifiers.joinToString(", ")}
                Parameters: ${parameters.joinToString { "${it.name}: ${it.type}" }}
                Exception Types: ${exceptionsTypes.joinToString(", ")}
                Annotations: ${annotations.joinToString(", ")}
            """.trimIndent()
        } else {
            """
                Method Name: $name
                Return Type: $type
                Declaring Class: ${declaringClass?.name}
                Modifiers: ${modifiers.joinToString(", ")}
                Parameters: ${parameters.joinToString { it.toString() }}
                Exception Types: ${exceptionsTypes.joinToString(", ")}
                Annotations: ${annotations.joinToString(", ")}
            """.trimIndent()
        }
    }
}