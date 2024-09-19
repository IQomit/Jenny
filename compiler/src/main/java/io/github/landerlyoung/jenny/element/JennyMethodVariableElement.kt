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

import javax.lang.model.element.ExecutableElement

internal class JennyMethodVariableElement(private val method: ExecutableElement) : JennyMethodElement {
    override val name: String
        get() = method.simpleName.toString()
    override val type: String
        get() = method.returnType.toString()
    override val annotations: List<String>
        get() = method.annotationMirrors.map { it.annotationType.toString() }
    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(method.modifiers)
    override val declaringClass: String?
        get() = null
    override val parameters: List<JennyParameter>
        get() = method.parameters.map { JennyParameter(it.simpleName.toString(), it.asType().toString()) }
    override val exceptionsTypes: List<String>
        get() = method.thrownTypes.map { it.toString() }
}