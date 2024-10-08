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

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

internal class JennyReflectType(private val type: Type) : JennyType {

    override val typeName: String
        get() = type.typeName

    override val jennyKind: JennyKind
        get() = JennyKind.fromReflectionType(type)

    override fun isPrimitive(): Boolean {
        return jennyKind in setOf(
            JennyKind.BOOLEAN, JennyKind.BYTE, JennyKind.CHAR,
            JennyKind.SHORT, JennyKind.INT, JennyKind.LONG,
            JennyKind.FLOAT, JennyKind.DOUBLE
        )
    }

    override fun isArray() = jennyKind == JennyKind.ARRAY

    override val componentType: JennyType?
        get() = when (type) {
            is Class<*> -> type.componentType?.let { JennyReflectType(it) }
            is GenericArrayType -> JennyReflectType(type.genericComponentType)
            else -> null
        }

    override fun getNonGenericType(): JennyType {
        return when (type) {
            is ParameterizedType -> JennyReflectType(type.rawType as Class<*>)
            is TypeVariable<*> -> {
                val bounds = type.bounds
                JennyReflectType(bounds.firstOrNull() ?: Any::class.java)
            }
            is WildcardType -> JennyReflectType(type.upperBounds.firstOrNull() ?: Any::class.java)
            else -> this
        }
    }
}