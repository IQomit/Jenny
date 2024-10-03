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

import gg.jte.TemplateEngine
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.generator.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.ProxyConfiguration

internal class TemplateJennyProxyHeaderDefinitionsProvider(private val templateEngine: TemplateEngine) :
    JennyProxyHeaderDefinitionsProvider {
    override fun getConstantsIdDeclare(constants: Collection<JennyVarElement>): String {
        TODO("Not yet implemented")
    }

    override fun getProxyHeaderInit(
        proxyConfiguration: ProxyConfiguration,
        startOfNamespace: String,
        classInfo: ClassInfo
    ): String {
        TODO("Not yet implemented")
    }

    override fun getProxyHeaderClazzInit(): String {
        TODO("Not yet implemented")
    }

    override fun getMethodOverloadPostfix(method: JennyExecutableElement): String {
        TODO("Not yet implemented")
    }

    override fun getConstructorsDefinitions(
        simpleClassName: String,
        constructors: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String {
        TODO("Not yet implemented")
    }

    override fun getMethodsDefinitions(methods: Map<JennyExecutableElement, Int>, useJniHelper: Boolean): String {
        TODO("Not yet implemented")
    }

    override fun getFieldsDefinitions(
        fields: Collection<JennyVarElement>,
        allMethods: Collection<JennyExecutableElement>,
        useJniHelper: Boolean,
        getterSetterForAllFields: Boolean,
        generateGetterForFields: Boolean,
        generateSetterForFields: Boolean
    ): String {
        TODO("Not yet implemented")
    }

    override fun generateForJniHelper(simpleClassName: String): String {
        TODO("Not yet implemented")
    }

    override fun initPreDefinition(isThreadSafe: Boolean): String {
        TODO("Not yet implemented")
    }

    override fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement, Int>): String {
        TODO("Not yet implemented")
    }

    override fun getMethodIdDeclare(methods: Map<JennyExecutableElement, Int>): String {
        TODO("Not yet implemented")
    }

    override fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String {
        TODO("Not yet implemented")
    }

    override fun initPostDefinition(endNamespace: String): String {
        TODO("Not yet implemented")
    }

}