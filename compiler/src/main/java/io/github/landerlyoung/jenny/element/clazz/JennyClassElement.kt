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
import io.github.landerlyoung.jenny.element.field.JennyFieldElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.model.JennyModifier
import io.github.landerlyoung.jenny.element.method.JennyExecutableReflectElement
import java.lang.reflect.Type

internal class JennyClassElement(private val clazz: Class<*>) : JennyClazzElement {
    override val name: String
        get() = clazz.simpleName
    override val fullClassName: String
        get() = clazz.canonicalName
    override val type: Type
        get() = object : Type {
            override fun getTypeName(): String = clazz.name
        }
    override val annotations: List<String>
        get() = clazz.annotations.map { it.annotationClass.simpleName ?: "Unknown" }
    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromReflectionModifiers(clazz.modifiers)
    override val declaringClass: String?
        get() = clazz.declaringClass?.name

    override val constructors: List<JennyExecutableElement>
        get() = clazz.constructors.map { JennyExecutableReflectElement(it) }
    override val methods: List<JennyExecutableElement>
        get() = clazz.methods.map { JennyExecutableReflectElement(it) }
    override val fields: List<JennyElement>
        get() = clazz.fields.map { JennyFieldElement(it) }

    override fun call(instance: Any?, vararg args: Any?): Any? {
        TODO("Not yet implemented")
    }
}