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
import java.util.*

internal class TemplateJennyProxyHeaderDefinitionsProvider(private val templateEngine: TemplateEngine) :
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
            "header_preamble.kte",
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

    override fun getProxyHeaderClazzInit(): String {
        return getFromTemplate("header_initfunctions.kte", emptyMap())
    }

    override fun getConstructorsDefinitions(
        simpleClassName: String,
        cppClassName: String,
        constructors: Map<JennyExecutableElement, Int>,
        useJniHelper: Boolean
    ): String {
        val returnType = if (useJniHelper) cppClassName else "jobject"
        return getFromTemplate(
            "constructors_definitions.kte",
            mapOf(
                "simpleClassName" to simpleClassName,
                "constructors" to constructors,
                "useJniHelper" to useJniHelper,
                "returnType" to returnType,
                "parametersProvider" to parametersProvider,
                "methodPrologue" to getJniMethodPrologue(useJniHelper),
            )
        )

    }

    private fun getJniMethodPrologue(
        useJniHelper: Boolean,
        isStatic: Boolean = true
    ): String {
        return getFromTemplate(
            "method_prologue.kte",
            mapOf(
                "useJniHelper" to useJniHelper,
                "isStatic" to isStatic,
            )
        )
    }

    override fun getMethodsDefinitions(methods: Map<JennyExecutableElement, Int>, useJniHelper: Boolean): String {
        return getFromTemplate(
            "methods_definitions.kte",
            mapOf(
                "methods" to methods,
                "useJniHelper" to useJniHelper,
                "parametersProvider" to parametersProvider,
                "methodPrologue" to getJniMethodPrologue(useJniHelper),
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
            "fields_definitions.kte",
            mapOf(
                "fields" to fields,
                "hasGetter" to hasGetter,
                "hasSetter" to hasSetter,
                "useJniHelper" to useJniHelper,
                "methodPrologue" to getJniMethodPrologue(useJniHelper),
                "parametersProvider" to parametersProvider
            )
        )
    }

    override fun generateForJniHelper(cppClassName: String): String {
        return getFromTemplate("jni_helper.kte", mapOf("cppClassName" to cppClassName))
    }

    override fun initPreDefinition(isThreadSafe: Boolean): String {
        return getFromTemplate("header_initvars.kte", mapOf("isThreadSafe" to isThreadSafe))
    }

    override fun getConstructorIdDeclare(constructors: Map<JennyExecutableElement, Int>): String {
        return getFromTemplate("constructors_ids_declarations.kte", mapOf("constructors" to constructors))
    }

    override fun getMethodIdDeclare(methods: Map<JennyExecutableElement, Int>): String {
        return getFromTemplate("methods_ids_declarations.kte", mapOf("methods" to methods))
    }

    override fun getFieldIdDeclare(fields: Collection<JennyVarElement>): String {
        return getFromTemplate("fields_ids_declarations.kte", mapOf("fields" to fields))
    }

    override fun initPostDefinition(endNamespace: String): String {
        return getFromTemplate("header_postamble.kte", mapOf("endNamespace" to endNamespace))
    }

    private fun getFromTemplate(templateName: String, mapOfVariables: Map<String, Any>): String {
        val templateOutput = StringOutput()
        templateEngine.render(templateName, mapOfVariables, templateOutput)
        return templateOutput.toString()
    }
}