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
package io.github.landerlyoung.jenny

import io.github.landerlyoung.jenny.utils.AnnotationResolver
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class JennyAnnotationProcessor : AbstractProcessor() {

    private lateinit var jennyProcessor: GenerationProcessorAPI<Any>
    private lateinit var jennyConfigurations: JennyProcessorConfiguration

    private lateinit var messager: Messager
    private lateinit var typeUtils: Types

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        typeUtils = processingEnv.typeUtils
        jennyConfigurations = JennyProcessorConfiguration.fromOptions(processingEnv.options)

        messager.printMessage(Diagnostic.Kind.NOTE, "Jenny configured with:${jennyConfigurations}")

        jennyProcessor = GenerationProcessorAPIImpl(jennyConfigurations)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.errorRaised()
            || roundEnv.processingOver()
            || !annotations.any { it.qualifiedName.toString() in SUPPORTED_ANNOTATIONS }
        ) return false

        try {
            generateNativeGlueCode(roundEnv)
            generateNativeProxy(roundEnv)
        } catch (e: Throwable) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Jenny failed to process ${e.javaClass.name} ${e.message}"
            )
        }

        return true
    }

    private fun generateNativeGlueCode(roundEnv: RoundEnvironment) {
        return roundEnv.getElementsAnnotatedWith(NativeClass::class.java)
            .filterIsInstance<TypeElement>()
            .forEach {
                jennyProcessor.setGlueNamespace(jennyConfigurations.glueNamespace)
                jennyProcessor.processForGlue(it)
            }
    }

    private fun generateNativeProxy(roundEnv: RoundEnvironment) {

        roundEnv.getElementsAnnotatedWith(NativeProxy::class.java)
            .forEach {
                val annotation = it.getAnnotation(NativeProxy::class.java)
                    ?: AnnotationResolver.getDefaultImplementation(NativeProxy::class.java)

                val proxyConfiguration = jennyConfigurations.provideProxyConfiguration().copy(
                    namespace = annotation.namespace,
                    allFields = annotation.allFields,
                    allMethods = annotation.allMethods,
                    onlyPublicMethod = false,
                )
                jennyProcessor.setProxyConfiguration(proxyConfiguration)
                jennyProcessor.processForProxy(it as TypeElement)
            }

        (roundEnv.getElementsAnnotatedWith(NativeProxyForClasses::class.java)
            .asSequence()
            .map { it.getAnnotation(NativeProxyForClasses::class.java) }
                +
                roundEnv.getElementsAnnotatedWith(NativeProxyForClasses.RepeatContainer::class.java)
                    .asSequence()
                    .flatMap {
                        it.getAnnotationsByType(NativeProxyForClasses::class.java).asSequence()
                    }
                )
            .toCollection(mutableSetOf())
            .forEach { annotation ->
                try {
                    annotation.classes
                    throw AssertionError("unreachable")
                } catch (e: MirroredTypesException) {
                    e.typeMirrors
                }.forEach {

                    val proxyConfiguration = jennyConfigurations.provideProxyConfiguration().copy(
                        namespace = annotation.namespace,
                        allFields = true,
                        allMethods = true,
                        onlyPublicMethod = true
                    )

                    jennyProcessor.setProxyConfiguration(proxyConfiguration)
                    jennyProcessor.processForProxy(typeUtils.asElement(it) as TypeElement)
                }
            }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return SUPPORTED_ANNOTATIONS
    }

    override fun getSupportedOptions(): Set<String> {
        return JennyProcessorConfiguration.configurationOptions
    }

    companion object {
        private val SUPPORTED_ANNOTATIONS: Set<String> = setOf(
            NativeClass::class.java.name,
            NativeCode::class.java.name,
            NativeFieldProxy::class.java.name,
            NativeMethodProxy::class.java.name,
            NativeProxy::class.java.name,
            NativeProxyForClasses::class.java.name
        )
    }
}