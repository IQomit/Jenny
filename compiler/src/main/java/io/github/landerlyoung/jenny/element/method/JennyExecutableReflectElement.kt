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

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.clazz.JennyClassElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.JennyParameter
import io.github.landerlyoung.jenny.element.model.type.JennyReflectType
import io.github.landerlyoung.jenny.element.model.type.JennyType
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal class JennyExecutableReflectElement(private val executable: Executable) : JennyExecutableElement {

    private constructor(executable: Executable, customName: String) : this(executable) {
        this.customName = customName
    }
    private var customName: String? = null

    override fun isConstructor() = executable is Constructor<*>

    override val name: String
        get() = customName ?: if (isConstructor()) "<init>" else executable.name

    override val type: JennyType
        get() = returnType

    override val returnType: JennyType
        get() = if (executable is Method) {
            JennyReflectType(executable.returnType)
        } else {
            JennyReflectType(Void.TYPE)
        }

    override fun withNewName(newName: String): JennyExecutableElement {
        return JennyExecutableReflectElement(executable, newName)
    }

    override val annotations: List<String>
        get() = executable.annotations.map { it.annotationClass.simpleName ?: "Unknown" }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromReflectionModifiers(executable.modifiers)

    override val declaringClass: JennyElement
        get() = JennyClassElement(executable.declaringClass)

    override val parameters: List<JennyParameter>
        get() = executable.parameters.map { JennyParameter(it.name, JennyReflectType(it.type)) }

    override val exceptionsTypes: List<String>
        get() = executable.exceptionTypes.map { it.name }

    override fun call(instance: Any?, vararg args: Any?): Any? {
        return try {
            executable.isAccessible = true
            when (executable) {
                is Method -> {
                    if (JennyModifier.STATIC in modifiers) {
                        executable.invoke(null, *args)
                    } else {
                        requireNotNull(instance) { "Instance must not be null for non-static field $name." }
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
}