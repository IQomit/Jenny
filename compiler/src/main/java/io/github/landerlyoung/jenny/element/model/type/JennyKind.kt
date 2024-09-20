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

import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

//todo: Finish this
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
    DECLARED;

    companion object {
        fun fromReflectionType(type: Type): JennyKind {
            return when (type) {
                is Class<*> -> {
                    when (type) {
                        Boolean::class.javaObjectType -> BOOLEAN
                        Byte::class.javaObjectType -> BYTE
                        Short::class.javaObjectType -> SHORT
                        Int::class.javaObjectType -> INT
                        Long::class.javaObjectType -> LONG
                        Float::class.javaObjectType -> FLOAT
                        Double::class.javaObjectType -> DOUBLE
                        Char::class.javaObjectType -> CHAR
                        Array::class.javaObjectType -> ARRAY
                        else -> VOID
                    }
                }

                is WildcardType -> DECLARED
                is GenericArrayType -> ARRAY
                else -> VOID
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
                else -> DECLARED
            }
        }
    }
}