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

package io.github.landerlyoung.jenny.element.model.type

import java.lang.reflect.Type

internal class JennyReflectType(private val type: Type) : JennyType {

    override val typeName: String
        get() = type.typeName

    override val jennyKind: JennyKind
        get() = JennyKind.fromReflectionType(type)

    override fun getComponentType(): JennyType? {
        val clazz = type as? Class<*>
        return clazz?.componentType?.let { JennyReflectType(it) }
    }
}