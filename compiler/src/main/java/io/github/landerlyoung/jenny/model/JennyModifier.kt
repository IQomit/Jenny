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

package io.github.landerlyoung.jenny.model

internal enum class JennyModifier {
    PUBLIC,
    PRIVATE,
    DEFAULT,
    STRICT,
    PROTECTED,
    STATIC,
    FINAL,
    VOLATILE,
    TRANSIENT,
    NATIVE,
    ABSTRACT,
    SYNCHRONIZED;

    companion object {
        fun fromReflectionModifiers(modifiers: Int): Set<JennyModifier> {
            return mutableSetOf<JennyModifier>().apply {
                if (java.lang.reflect.Modifier.isPublic(modifiers)) add(PUBLIC)
                if (java.lang.reflect.Modifier.isPrivate(modifiers)) add(PRIVATE)
                if (java.lang.reflect.Modifier.isProtected(modifiers)) add(PROTECTED)
                if (java.lang.reflect.Modifier.isStatic(modifiers)) add(STATIC)
                if (java.lang.reflect.Modifier.isFinal(modifiers)) add(FINAL)
                if (java.lang.reflect.Modifier.isVolatile(modifiers)) add(VOLATILE)
                if (java.lang.reflect.Modifier.isTransient(modifiers)) add(TRANSIENT)
                if (java.lang.reflect.Modifier.isAbstract(modifiers)) add(ABSTRACT)
                if (java.lang.reflect.Modifier.isNative(modifiers)) add(NATIVE)
                if (java.lang.reflect.Modifier.isSynchronized(modifiers)) add(SYNCHRONIZED)
                if (java.lang.reflect.Modifier.isStrict(modifiers)) add(STRICT)
            }
        }

        fun fromElementModifiers(modifiers: Set<javax.lang.model.element.Modifier>): Set<JennyModifier> {
            return modifiers.mapNotNull {
                when (it) {
                    javax.lang.model.element.Modifier.PUBLIC -> PUBLIC
                    javax.lang.model.element.Modifier.PRIVATE -> PRIVATE
                    javax.lang.model.element.Modifier.PROTECTED -> PROTECTED
                    javax.lang.model.element.Modifier.STATIC -> STATIC
                    javax.lang.model.element.Modifier.FINAL -> FINAL
                    javax.lang.model.element.Modifier.ABSTRACT -> ABSTRACT
                    javax.lang.model.element.Modifier.VOLATILE -> VOLATILE
                    javax.lang.model.element.Modifier.TRANSIENT -> TRANSIENT
                    javax.lang.model.element.Modifier.NATIVE -> NATIVE
                    javax.lang.model.element.Modifier.SYNCHRONIZED -> SYNCHRONIZED
                    javax.lang.model.element.Modifier.DEFAULT -> DEFAULT
                    javax.lang.model.element.Modifier.STRICTFP -> STRICT
                    else -> null  // TODO: Handle sealed or non sealed modifiers if needed
                }
            }.toSet()
        }
    }
}