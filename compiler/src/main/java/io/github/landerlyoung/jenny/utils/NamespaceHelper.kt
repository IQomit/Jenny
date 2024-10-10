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

package io.github.landerlyoung.jenny.utils

/*
 * ```
 * Author: landerlyoung@gmail.com
 * Date:   2019-09-27
 * Time:   11:17
 * Life with Passion, Code with Creativity.
 * ```
 */
class NamespaceHelper(private var namespace: String = "") {
    private val namespaces: List<String>
        get() = namespace.split("::").map { it.trim() }
            .filter { it.isNotEmpty() }

    val fileNamePrefix: String
        get() = namespaces.joinToString("_").let {
            if (it.isNotEmpty()) {
                it + "_"
            } else {
                it
            }
        }

    // like std::chrono
    val namespaceNotation: String
        get() = namespaces.joinToString("::")

    fun beginNamespace() = namespaces.joinToString(" ") { "namespace $it {" }

    fun endNamespace() = if (namespaces.isNotEmpty())
        namespaces.joinToString(" ", postfix = " // endof namespace ${namespaces.joinToString("::") { it }}") { "}" }
    else ""

    fun assignNamespace(namespace: String) {
        this.namespace = namespace
    }

    fun getNamespace() = namespace
}