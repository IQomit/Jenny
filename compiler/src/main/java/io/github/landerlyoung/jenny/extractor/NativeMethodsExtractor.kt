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

package io.github.landerlyoung.jenny.extractor

import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod

internal class NativeMethodsExtractor : Extractor<KClass<*>, Sequence<KFunction<*>>> {

    override fun extract(input: KClass<*>): Sequence<KFunction<*>> {
        return input.declaredFunctions.asSequence()
            .filter { function ->
                val javaMethod = function.javaMethod
                javaMethod != null && Modifier.isNative(javaMethod.modifiers) || function.isExternal
            }.map { it }
    }
}