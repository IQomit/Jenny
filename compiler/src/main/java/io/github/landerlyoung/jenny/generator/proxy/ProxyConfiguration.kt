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

package io.github.landerlyoung.jenny.generator.proxy

internal data class ProxyConfiguration(
    val threadSafe: Boolean,
    val useJniHelper: Boolean,
    val headerOnlyProxy: Boolean,

    val namespace:String,
    val allFields: Boolean,
    val onlyPublicMethod: Boolean,
    val gettersForFields: Boolean,
    val settersForFields: Boolean,
)