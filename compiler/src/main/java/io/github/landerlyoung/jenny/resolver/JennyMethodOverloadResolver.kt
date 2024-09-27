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

internal class JennyMethodOverloadResolver(private val resolver: MethodParameterResolver = MethodParameterResolver()) :
    Resolver<Collection<JennyExecutableElement>, Collection<JennyMethodRecord>> {
    override fun resolve(
        input: Collection<JennyExecutableElement>
    ): Collection<JennyMethodRecord> {
        val duplicateRecord = mutableMapOf<String, Boolean>()
        input.forEach {
            val p = resolver.resolve(it)
            duplicateRecord[p] = duplicateRecord.containsKey(p)
        }

        return input.mapIndexed { index, method ->
            val p = resolver.resolve(method)
            if (duplicateRecord[p]!! || Constants.CPP_RESERVED_WORS.contains(method.name)) {
                JennyMethodRecord(method, JennyHeaderDefinitionsProvider.getMethodOverloadPostfix(method), index)
            } else {
                JennyMethodRecord(method = method, index = index)
            }
        }
    }
}