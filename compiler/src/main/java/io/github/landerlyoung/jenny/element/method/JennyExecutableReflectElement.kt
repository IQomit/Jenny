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
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type

internal class JennyExecutableReflectElement(private val executable: Executable) : JennyExecutableElement {
    override val name: String
        get() = if (executable is Constructor<*>) "<init>" else executable.name

    override val type: Type
        get() = when (executable) {
            is Method -> executable.genericReturnType
            else -> executable.declaringClass.componentType
        }

    override val returnType: Type
        get() = type

    override val annotations: List<String>
        get() = executable.annotations.map { it.annotationClass.simpleName ?: "Unknown" }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromReflectionModifiers(executable.modifiers)

    override val declaringClass: String?
        get() = executable.declaringClass.name

    override val parameters: List<JennyParameter>
        get() = executable.parameters.map { JennyParameter(it.name,it.type) }

    override val exceptionsTypes: List<String>
        get() = executable.exceptionTypes.map { it.name }

    override fun call(instance: Any?, vararg args: Any?): Any? {
        return try {
            executable.isAccessible = true
            when (executable) {
                is Method -> {
                    if (modifiers.find { it == JennyModifier.STATIC } != null) {
                        executable.invoke(null, *args)
                    } else {
                        executable.invoke(instance, *args)
                    }
                }
                is Constructor<*> -> executable.newInstance(*args) // For constructor calls
                else -> throw UnsupportedOperationException("Unsupported executable type")
            }
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Unable to access executable: $name", e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Invalid arguments passed to executable: $name", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Exception thrown by executable: $name", e.cause)
        }
    }

    override fun describe(): String {
        return if (executable is Constructor<*>) {
            """
                Constructor for: $declaringClass
                Modifiers: ${modifiers.joinToString(", ")}
                Parameters: ${parameters.joinToString { "${it.name}: ${it.type}" }}
                Exception Types: ${exceptionsTypes.joinToString(", ")}
                Annotations: ${annotations.joinToString(", ")}
            """.trimIndent()
        } else {
            """
                Method Name: $name
                Return Type: $type
                Declaring Class: $declaringClass
                Modifiers: ${modifiers.joinToString(", ")}
                Parameters: ${parameters.joinToString { "${it.name}: ${it.type}" }}
                Exception Types: ${exceptionsTypes.joinToString(", ")}
                Annotations: ${annotations.joinToString(", ")}
            """.trimIndent()
        }
    }
}