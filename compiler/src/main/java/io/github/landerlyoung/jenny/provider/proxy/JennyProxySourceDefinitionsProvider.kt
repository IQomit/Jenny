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
package io.github.landerlyoung.jenny.provider.proxy

import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.provider.Provider

internal interface JennyProxySourceDefinitionsProvider : Provider {
    val autoGenerateNotice: String
    fun generateSourcePreContent(
        headerFileName: String,
        startOfNamespace: String,
        simpleClassName: String,
        errorLoggerFunction: String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String

    fun generateSourcePostContent(
        simpleClassName: String,
        endNamespace: String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String

    fun getConstructorIdInit(constructors: Map<JennyExecutableElement, Int>): String
    fun getMethodIdInit(methods: Map<JennyExecutableElement, Int>): String
    fun getFieldIdInit(fields: Collection<JennyVarElement>): String
}