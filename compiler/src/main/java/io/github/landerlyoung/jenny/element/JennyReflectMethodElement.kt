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

package io.github.landerlyoung.jenny.element

import java.lang.reflect.Method

internal class JennyReflectMethodElement(private val method: Method) : JennyMethodElement {
    override val name: String
        get() = method.name
    override val type: String
        get() = method.returnType.name
    override val annotations: List<String>
        get() = method.annotations.map { it.annotationClass.simpleName ?: "Unknown" }
    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromFieldModifiers(method.modifiers)
    override val declaringClass: String?
        get() = method.declaringClass.name
    override val parameters: List<JennyParameter>
        get() = method.parameters.map { JennyParameter(it.name, it.type.name) }
    override val exceptionsTypes: List<String>
        get() = method.exceptionTypes.map { it.name }

    override fun call(instance: Any?): Any {
        method.isAccessible = true
        return method.invoke(instance)
    }

    override fun describe(): String {
        TODO("Not yet implemented")
    }
}