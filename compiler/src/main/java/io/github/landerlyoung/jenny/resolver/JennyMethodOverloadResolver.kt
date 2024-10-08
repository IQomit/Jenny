/**
 * Copyright (C) 2024 The Qt Company Ltd.
 * Copyright 2016 landerlyoung@gmail.com
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

package io.github.landerlyoung.jenny.resolver

import io.github.landerlyoung.jenny.Constants
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.utils.Signature
import io.github.landerlyoung.jenny.utils.stripNonASCII

internal class JennyMethodOverloadResolver(
    private val resolver: MethodParameterResolver = MethodParameterResolver()
) : Resolver<Collection<JennyExecutableElement>, Map<JennyExecutableElement, Int>> {

    private val reservedWords = Constants.CPP_RESERVED_WORS

    override fun resolve(
        input: Collection<JennyExecutableElement>
    ): Map<JennyExecutableElement, Int> {
        val overloadMap = mutableMapOf<String, Boolean>()
        val nameCountMap = mutableMapOf<String, Int>()
        val resultMap = mutableMapOf<JennyExecutableElement, Int>()

        input.forEach { method ->
            val paramSignature = resolver.resolve(method)
            overloadMap[paramSignature] = overloadMap.containsKey(paramSignature)
        }

        input.forEach { method ->
            val methodName = method.name
            val currentCount = nameCountMap.getOrDefault(methodName, 0)
            nameCountMap[methodName] = currentCount + 1

            val paramSignature = resolver.resolve(method)
            val isOverloaded = overloadMap[paramSignature] == true
            val isCppReserved = reservedWords.contains(method.name)
            val updatedMethod = if (isOverloaded || isCppReserved) {
                val postfix = getMethodOverloadPostfix(method)
                JennyExecutableElement.createWithNewName(
                    method,
                    if (method.isConstructor()) "newInstance${postfix}" else "${methodName}_${postfix}"
                )
            } else {
                if (method.isConstructor())
                    JennyExecutableElement.createWithNewName(method, "newInstance")
                else method
            }
            resultMap[updatedMethod] = currentCount
        }

        return resultMap
    }

    private fun getMethodOverloadPostfix(method: JennyExecutableElement): String {
        val signature = Signature.getBinaryJennyElementSignature(method)
        val paramSig =
            signature.subSequence(signature.indexOf('(') + 1, signature.indexOf(")")).toString()
        return "__" + paramSig.replace("_", "_1")
            .replace("/", "_")
            .replace(";", "_2")
            .stripNonASCII()
    }
}