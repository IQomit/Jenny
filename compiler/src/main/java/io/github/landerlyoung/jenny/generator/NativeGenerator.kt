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

package io.github.landerlyoung.jenny.generator

import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

internal class NativeGenerator : Generator<KClass<*>, Unit> {

    private val nativeHeaderGenerator = NativeHeaderGenerator()
    private val nativeSourceGenerator = NativeSourceGenerator()

    override fun generate(input: KClass<*>) {
        val classInfo = extractClassInfo(input)
        val nativeMethods = extractNativeMethods(input)
        val constants = extractConstants(input)
        val headerContent = nativeHeaderGenerator.generate(HeaderData(classInfo, nativeMethods, constants))
        val sourceContent = nativeSourceGenerator.generate(classInfo to nativeMethods)
    }

    private fun extractClassInfo(input: KClass<*>): ClassInfo {
        TODO("Not yet implemented")
    }

    private fun extractNativeMethods(clazz: KClass<*>): Sequence<KFunction<*>> {
        return clazz.declaredFunctions.asSequence()
            .filter { function ->
                val javaMethod = function.javaMethod
                javaMethod != null && Modifier.isNative(javaMethod.modifiers) || function.isExternal
            }.map { it }
    }

    private fun extractConstants(clazz: KClass<*>): Sequence<KProperty1<out Any, *>> {
        return clazz.memberProperties.asSequence()
            .filter { property ->
                val javaField = property.javaField
                javaField != null &&
                        Modifier.isStatic(javaField.modifiers) &&
                        Modifier.isFinal(javaField.modifiers)
            }
    }
}