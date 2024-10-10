package io.github.landerlyoung.jenny.provider.glue

import io.github.landerlyoung.jenny.element.field.JennyVarElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.generator.model.ClassInfo
import io.github.landerlyoung.jenny.provider.Provider

internal interface JennyGlueHeaderDefinitionsProvider : Provider {
    fun getHeaderInitForGlue(classInfo: ClassInfo, startOfNamespace: String): String
    fun getConstantsIdDeclare(constants: Collection<JennyVarElement>): String
    fun getEndNameSpace(className: String, endNamespace: String, isSource: Boolean = false): String
    fun getNativeMethodsDefinitions(
        classInfo: ClassInfo,
        methods: Collection<JennyExecutableElement>,
        isSource: Boolean = false
    ): String

    fun getJniRegister(methods: Collection<JennyExecutableElement>): String

}