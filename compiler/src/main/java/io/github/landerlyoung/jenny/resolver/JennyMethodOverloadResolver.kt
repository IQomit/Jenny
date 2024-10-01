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
import io.github.landerlyoung.jenny.utils.JennyHeaderDefinitionsProvider

internal class JennyMethodOverloadResolver(
    private val resolver: MethodParameterResolver = MethodParameterResolver()
) : Resolver<Collection<JennyExecutableElement>, Collection<JennyMethodRecord>> {

    override fun resolve(
        input: Collection<JennyExecutableElement>
    ): Collection<JennyMethodRecord> {
        val overloadMap = mutableMapOf<String, Boolean>()

        input.forEach { method ->
            val paramSignature = resolver.resolve(method)
            overloadMap[paramSignature] = overloadMap.containsKey(paramSignature)
        }

        return input.mapIndexed { index, method ->
            val paramSignature = resolver.resolve(method)
            val isOverloaded = overloadMap[paramSignature] == true
            val isCppReserved = Constants.CPP_RESERVED_WORS.contains(method.name)

            if (isOverloaded || isCppReserved) {
                val postfix = JennyHeaderDefinitionsProvider.getMethodOverloadPostfix(method)
                JennyMethodRecord(method, postfix, index)
            } else {
                JennyMethodRecord(method = method, index = index)
            }
        }
    }
}