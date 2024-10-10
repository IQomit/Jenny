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

package io.github.landerlyoung.jenny.utils

import io.github.landerlyoung.jenny.generator.model.Namespace

internal class CppFileHelper  {
    private val namespaceHelper = NamespaceHelper()

    val jniHelperDefaultName:String
        get() = "jnihelper.h"

    val namespaceNotation :String
        get() = namespaceHelper.namespaceNotation
    fun provideHeaderFile(className: String) = "${namespaceHelper.fileNamePrefix}$className.h"
    fun provideSourceFile(className: String) = "${namespaceHelper.fileNamePrefix}$className.cpp"
    fun provideNamespace(): Namespace {
        return Namespace(
            startOfNamespace = namespaceHelper.beginNamespace(),
            endOfNameSpace = namespaceHelper.endNamespace()
        )
    }

    fun setNamespace(namespace: String) {
        namespaceHelper.assignNamespace(namespace)
    }
}