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

import java.lang.reflect.*
import java.lang.reflect.Array
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

internal enum class JennyKind {
    VOID,
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    CHAR,
    DOUBLE,
    ARRAY,
    NULL,
    DECLARED,
    UNKNOWN;

    companion object {
        fun fromReflectionType(type: Type): JennyKind {
            return when (type) {
                is Class<*> -> {
                    when (type) {
                        Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> BOOLEAN
                        Byte::class.javaPrimitiveType, Byte::class.javaObjectType -> BYTE
                        Short::class.javaPrimitiveType, Short::class.javaObjectType -> SHORT
                        Int::class.javaPrimitiveType, Int::class.javaObjectType -> INT
                        Long::class.javaPrimitiveType, Long::class.javaObjectType -> LONG
                        Float::class.javaPrimitiveType, Float::class.javaObjectType -> FLOAT
                        Double::class.javaPrimitiveType, Double::class.javaObjectType -> DOUBLE
                        Char::class.javaPrimitiveType, Char::class.javaObjectType -> CHAR
                        Array::class.java -> ARRAY
                        Void.TYPE -> VOID
                        else -> DECLARED
                    }
                }

                is GenericArrayType -> ARRAY
                is WildcardType, is ParameterizedType -> DECLARED
                else -> UNKNOWN
            }
        }

        fun fromMirrorType(type: TypeMirror): JennyKind {
            return when (type.kind) {
                TypeKind.VOID -> VOID
                TypeKind.BOOLEAN -> BOOLEAN
                TypeKind.BYTE -> BYTE
                TypeKind.SHORT -> SHORT
                TypeKind.INT -> INT
                TypeKind.LONG -> LONG
                TypeKind.CHAR -> CHAR
                TypeKind.DOUBLE -> DOUBLE
                TypeKind.ARRAY -> ARRAY
                TypeKind.NULL -> NULL
                TypeKind.DECLARED -> DECLARED
                else -> UNKNOWN
            }
        }
    }
}