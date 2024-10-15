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
import io.github.landerlyoung.jenny.generator.model.ClassInfo
import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration
import io.github.landerlyoung.jenny.provider.proxy.JennyProxyHeaderDefinitionsProvider
import io.github.landerlyoung.jenny.utils.FieldSetterGetterFinder
import io.github.landerlyoung.jenny.utils.ParametersProvider
import java.util.EnumSet

internal class QTemplateJennyProxyHeaderDefinitionsProvider(private val templateEngine: TemplateEngine) :
    JennyProxyHeaderDefinitionsProvider {
    private val parametersProvider = ParametersProvider()

    override val autoGenerateNotice: String
        get() = getFromTemplate("auto_generate_notice.kte", emptyMap())

    override fun getProxyHeaderInit(
        jennyProxyConfiguration: JennyProxyConfiguration,
        startOfNamespace: String,
        classInfo: ClassInfo
    ): String {
        return getFromTemplate(
            "qjni/qjni_header_preamble.kte",
            mapOf(
                "proxyConfiguration" to jennyProxyConfiguration,
                "startOfNamespace" to startOfNamespace,
                "cppClassName" to classInfo.cppClassName,
                "slashClassName" to classInfo.slashClassName,
            )
        )
    }

    override fun getConstantsIdDeclare(constants: Collection<JennyVarElement>): String {
        return getFromTemplate(
            "constants_ids_declarations.kte",
            mapOf("constants" to constants, "parametersProvider" to parametersProvider)
        )
    }

    override fun getProxyHeaderClazzInit(): String = ""

    override fun getConstructorsDefinitions(
        simpleClassName: String,
        cppClassName: String,
        constructors: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String {
        return getFromTemplate(
            "qjni/qjni_constructors_definitions.kte",
            mapOf(
                "simpleClassName" to simpleClassName,
                "constructors" to constructors,
                "useJniHelper" to useJniHelper,
                "returnType" to cppClassName,
                "parametersProvider" to parametersProvider,
            )
        )
    }

    override fun getMethodsDefinitions(
        methods: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String {
        return getFromTemplate(
            "qjni/qjni_methods_definitions.kte",
            mapOf(
                "methods" to methods,
                "useJniHelper" to useJniHelper,
                "parametersProvider" to parametersProvider,
            )
        )
    }

    override fun getFieldsDefinitions(
        fields: Collection<JennyVarElement>,
        allMethods: Collection<JennyExecutableElement>,
        useJniHelper: Boolean,
        getterSetterForAllFields: Boolean,
        generateGetterForField: (JennyVarElement) -> Boolean,
        generateSetterForField: (JennyVarElement) -> Boolean
    ): String {
        fun hasGetterSetter(field: JennyVarElement): EnumSet<FieldSetterGetterFinder.GetterSetter> {
            return FieldSetterGetterFinder.hasGetterSetter(
                field,
                allMethods,
                generateGetterForField(field),
                generateSetterForField(field),
                getterSetterForAllFields
            )
        }

        val hasGetter: (JennyVarElement) -> Boolean = { field ->
            hasGetterSetter(field).contains(FieldSetterGetterFinder.GetterSetter.GETTER)
        }

        val hasSetter: (JennyVarElement) -> Boolean = { field ->
            hasGetterSetter(field).contains(FieldSetterGetterFinder.GetterSetter.SETTER)
        }
        return getFromTemplate(
            "qjni/qjni_fields_definitions.kte",
            mapOf(
                "fields" to fields,
                "hasGetter" to hasGetter,
                "hasSetter" to hasSetter,
                "useJniHelper" to useJniHelper,
                "parametersProvider" to parametersProvider
            )
        )
    }

    override fun generateForJniHelper(cppClassName: String): String  = ""

    override fun initPreDefinition(isThreadSafe: Boolean): String = ""

    override fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement, Int>): String = ""

    override fun getMethodIdDeclare(methods: Map<JennyExecutableElement, Int>): String = ""

    override fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String = ""

    override fun initPostDefinition(endNamespace: String): String {
        return getFromTemplate("qjni/qjni_header_postamble.kte", mapOf("endNamespace" to endNamespace))
    }

    private fun getFromTemplate(templateName: String, mapOfVariables: Map<String, Any>): String {
        val templateOutput = StringOutput()
        templateEngine.render(templateName, mapOfVariables, templateOutput)
        return templateOutput.toString()
    }
}