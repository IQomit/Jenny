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

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.clazz.JennyClassTypeElement
import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.type.JennyMirrorType
import io.github.landerlyoung.jenny.element.model.type.JennyType
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

internal class JennyVariableElement(private val variableElement: VariableElement) : JennyVarElement {
    override val name: String
        get() = variableElement.simpleName.toString()

    override val type: JennyType
        get() = JennyMirrorType(variableElement.asType())

    override val annotations: List<String>
        get() = variableElement.annotationMirrors.map { it.annotationType.toString() }

    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return variableElement.getAnnotation(annotationClass)
    }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(variableElement.modifiers)

    override val declaringClass: JennyElement
        get() = JennyClassTypeElement(variableElement.enclosingElement as TypeElement)

    override fun call(instance: Any?, vararg args: Any?): Any? {
        val constantValue = variableElement.constantValue
        if (constantValue != null)
            return constantValue
        return try {
            val clazz = Class.forName((declaringClass as JennyClazzElement).fullClassName)
            val field = clazz.getDeclaredField(name).apply {
                isAccessible = true
            }
            if (JennyModifier.STATIC in modifiers) {
                field.get(null)
            } else {
                requireNotNull(instance) { "Instance must not be null for non-static field $name." }
                field.get(instance)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to access field '$name' in class '${declaringClass.name}'.", e)
        }
    }
}