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
package io.github.landerlyoung.jenny.provider.proxy.impl

import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.provider.proxy.JennyProxySourceDefinitionsProvider

class QTemplateJennyProxySourceDefinitionsProvider(private val templateEngine: TemplateEngine) :
    JennyProxySourceDefinitionsProvider {
    override val autoGenerateNotice: String
        get() = getFromTemplate("auto_generate_notice.kte", emptyMap())

    override fun generateSourcePreContent(
        headerFileName: String,
        startOfNamespace: String,
        cppClassName: String,
        errorLoggerFunction: String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String  = ""

    override fun generateSourcePostContent(
        cppClassName: String,
        endNamespace: String,
        headerOnly: Boolean,
        threadSafe: Boolean
    ): String  = ""

    override fun getConstructorIdInit(constructors: Map<JennyExecutableElement, Int>): String  = ""

    override fun getMethodIdInit(methods: Map<JennyExecutableElement, Int>): String  = ""

    override fun getFieldIdInit(fields: Collection<JennyVarElement>): String  = ""

    private fun getFromTemplate(templateName: String, mapOfVariables: Map<String, Any>): String {
        val templateOutput = StringOutput()
        templateEngine.render(templateName, mapOfVariables, templateOutput)
        return templateOutput.toString()
    }
}