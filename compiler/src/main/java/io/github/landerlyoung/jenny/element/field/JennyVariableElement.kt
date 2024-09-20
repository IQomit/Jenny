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
import java.lang.reflect.Type
import javax.lang.model.element.VariableElement

internal class JennyVariableElement(private val variableElement: VariableElement) : JennyVarElement {
    override val name: String
        get() = variableElement.simpleName.toString()

    override val type: Type
        get() = object : Type {
            override fun getTypeName(): String = variableElement.asType().toString()
        }

    override val annotations: List<String>
        get() = variableElement.annotationMirrors.map { it.annotationType.toString() }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(variableElement.modifiers)

    override val declaringClass: String?
        get() = null

    override fun call(instance: Any?, vararg args: Any?): Any? = variableElement.constantValue

    override fun describe(): String {
        return """
            Variable Name: $name
            Type: $type
            Modifiers: ${modifiers.joinToString(", ")}
            Annotations: ${annotations.joinToString(", ")}
        """.trimIndent()
    }
}