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
import io.github.landerlyoung.jenny.generator.model.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration
import io.github.landerlyoung.jenny.provider.Provider

internal interface JennyProxyHeaderDefinitionsProvider : Provider {
    val autoGenerateNotice: String
    fun getProxyHeaderInit(
        jennyProxyConfiguration: JennyProxyConfiguration,
        startOfNamespace: String,
        classInfo: ClassInfo
    ): String

    fun getConstantsIdDeclare(constants: Collection<JennyVarElement>): String

    fun getProxyHeaderClazzInit(): String
    fun getConstructorsDefinitions(
        simpleClassName: String,
        cppClassName:String,
        constructors: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String

    fun getMethodsDefinitions(
        methods: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String

    fun getFieldsDefinitions(
        fields: Collection<JennyVarElement>,
        allMethods: Collection<JennyExecutableElement>,
        useJniHelper: Boolean,
        getterSetterForAllFields: Boolean,
        generateGetterForField: (JennyVarElement) -> Boolean,
        generateSetterForField: (JennyVarElement) -> Boolean
    ): String

    fun generateForJniHelper(cppClassName: String): String
    fun initPreDefinition(isThreadSafe: Boolean): String
    fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement, Int>): String
    fun getMethodIdDeclare(methods: Map<JennyExecutableElement, Int>): String
    fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String
    fun initPostDefinition(endNamespace: String): String
}