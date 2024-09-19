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

import io.github.landerlyoung.jenny.model.JennyModifier
import io.github.landerlyoung.jenny.model.JennyParameter
import java.lang.reflect.Type
import javax.lang.model.element.ExecutableElement

internal class JennyExecutableVariableElement(private val method: ExecutableElement) : JennyExecutableElement {
    override val name: String
        get() = method.simpleName.toString()

    override val type: Type
        get() = object : Type {
            override fun getTypeName(): String = method.asType().toString()
        }

    override val returnType: Type
        get() = object : Type {
            override fun getTypeName(): String = method.returnType.toString()
        }

    override val annotations: List<String>
        get() = method.annotationMirrors.map { it.annotationType.toString() }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(method.modifiers)

    override val declaringClass: String
        get() = method.enclosedElements.toString()

    override val parameters: List<JennyParameter>
        get() = method.parameters.map {
            JennyParameter(it.simpleName.toString(), object : Type {
                override fun getTypeName(): String = it.asType().toString()
            })
        }

    override val exceptionsTypes: List<String>
        get() = method.thrownTypes.map { it.toString() }

    override fun call(instance: Any?, vararg args: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun describe(): String {
        TODO("Not yet implemented")
    }
}