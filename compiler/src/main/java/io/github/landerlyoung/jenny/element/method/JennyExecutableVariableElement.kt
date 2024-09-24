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
import io.github.landerlyoung.jenny.element.clazz.JennyClassTypeElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.JennyParameter
import io.github.landerlyoung.jenny.element.model.type.JennyMirrorType
import io.github.landerlyoung.jenny.element.model.type.JennyType
import java.lang.reflect.Type
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal class JennyExecutableVariableElement(private val method: ExecutableElement) : JennyExecutableElement {
    override val name: String
        get() = method.simpleName.toString()

    override val type: JennyType
        get() = JennyMirrorType(method.asType())

    override val returnType: JennyType
        get() = JennyMirrorType(method.returnType)

    override val annotations: List<String>
        get() = method.annotationMirrors.map { it.annotationType.toString() }

    override val modifiers: Set<JennyModifier>
        get() = JennyModifier.fromElementModifiers(method.modifiers)

    override val declaringClass: JennyElement
        get() = JennyClassTypeElement(method.enclosingElement as TypeElement)

    override val parameters: List<JennyParameter>
        get() = method.parameters.map {
            JennyParameter(it.simpleName.toString(), JennyMirrorType(it.asType()))
        }

    override val exceptionsTypes: List<String>
        get() = method.thrownTypes.map { it.toString() }

    override fun call(instance: Any?, vararg args: Any?): Any? {
        return try {
            val clazz = (instance?.javaClass ?: method.enclosingElement.asType().toString().let { Class.forName(it) })
            if (method.kind == ElementKind.CONSTRUCTOR) {
                val constructor = clazz.getConstructor(*args.map { it?.javaClass }.toTypedArray())
                constructor.isAccessible = true
                constructor.newInstance(*args) // Create a new instance
            } else {
                val runtimeMethod = clazz.getDeclaredMethod(name, *args.map { it?.javaClass }.toTypedArray())
                runtimeMethod.isAccessible = true
                if (modifiers.contains(JennyModifier.STATIC)) {
                    runtimeMethod.invoke(null, *args)
                } else {
                    runtimeMethod.invoke(instance, *args)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to invoke ${if (method.kind == ElementKind.CONSTRUCTOR) "constructor" else "method"}: $name",
                e
            )
        }
    }

    override fun describe(): String {
        return if (method.kind == ElementKind.CONSTRUCTOR) {
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
                Parameters: ${parameters.joinToString { it.toString() }}
                Exception Types: ${exceptionsTypes.joinToString(", ")}
                Annotations: ${annotations.joinToString(", ")}
            """.trimIndent()
        }
    }
}