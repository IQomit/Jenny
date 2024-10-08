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

package io.github.landerlyoung.jenny.element.clazz

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.field.JennyVariableElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableVariableElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.type.JennyMirrorType
import io.github.landerlyoung.jenny.element.model.type.JennyType
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

internal class JennyClassTypeElement(private val clazz: TypeElement) : JennyClazzElement {
    override val name: String
        get() = clazz.simpleName.toString()

    override val fullClassName: String
        get() = clazz.qualifiedName.toString()

    override val type: JennyType
        get() = JennyMirrorType(clazz.asType())

    override val isNestedClass: Boolean
        get() = clazz.javaClass.enclosingClass != null && !clazz.javaClass.isMemberClass

    override val annotations: List<String>
        get() = clazz.annotationMirrors.map { it.annotationType.toString() }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(clazz.modifiers)

    override val declaringClass: JennyElement?
        get() = if (isNestedClass) JennyClassTypeElement(clazz.enclosingElement as TypeElement) else null

    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return clazz.getAnnotation(annotationClass)
    }

    override val constructors: List<JennyExecutableElement>
        get() = clazz.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .map { JennyExecutableVariableElement(it) }

    override val methods: List<JennyExecutableElement>
        get() = clazz.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .filter { it.kind == ElementKind.METHOD }
            .map { JennyExecutableVariableElement(it) }

    override val fields: List<JennyVarElement>
        get() = clazz.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.kind == ElementKind.FIELD &&
                    !isCompanionField(it)  }
            .map { JennyVariableElement(it) }

    private fun isCompanionField(element: Element): Boolean {
        return element.simpleName.toString() == "Companion" ||
                (element.enclosingElement is TypeElement &&
                        element.enclosingElement.simpleName.toString() == "Companion")
    }
}