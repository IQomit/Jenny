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

package io.github.landerlyoung.jenny.generator.glue

import io.github.landerlyoung.jenny.generator.ClassInfo
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

internal data class HeaderData(
    val classInfo: ClassInfo,
    val methods: Collection<KFunction<*>>,
    val constants: Collection<KProperty1<out Any, *>>
)

internal data class SourceData(
    val headerFileName : String,
    val classInfo: ClassInfo,
    val methods: Collection<KFunction<*>>,
)