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

import io.github.landerlyoung.jenny.generator.proxy.JennyProxyConfiguration
import io.github.landerlyoung.jenny.utils.AnnotationResolver
import io.github.landerlyoung.jenny.utils.FileHandler
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * Author: landerlyoung@gmail.com
 * Date:   2014-12-16
 * Time:   19:42
 * Life with passion. Code with creativity!
 */
class JennyAnnotationProcessor : AbstractProcessor() {

    private lateinit var jennyProcessor: ProcessorAPI<Any>

    private lateinit var messager: Messager
    private lateinit var typeUtils: Types

    private lateinit var outputDirectory: String

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        typeUtils = processingEnv.typeUtils
        val configurations = Configurations.fromOptions(processingEnv.options)
        outputDirectory = configurations.outputDirectory!!
        messager.printMessage(
            Diagnostic.Kind.NOTE,
            "Jenny configured with:${configurations}"
        )
        val templatesPath = System.getProperty("user.dir") + "/compiler/src/main/resources/jte"
        jennyProcessor =
            ProcessorAPIImpl(
                outputDirectory = outputDirectory,
                templatesPath = templatesPath
            )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.errorRaised()
            || roundEnv.processingOver()
            || !annotations.any { it.qualifiedName.toString() in SUPPORTED_ANNOTATIONS }
        ) return false

        try {

            generateNativeGlueCode(roundEnv)
            generateNativeProxy(roundEnv)
//            generateFusionProxyHeader(proxyClasses)
            generateJniHelper()
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
                jennyProcessor.setGlueNamespace("")
                jennyProcessor.processForGlue(it)
            }
    }

    private fun generateNativeProxy(roundEnv: RoundEnvironment) {

        roundEnv.getElementsAnnotatedWith(NativeProxy::class.java)
            .map {
                val annotation = it.getAnnotation(NativeProxy::class.java)
                    ?: AnnotationResolver.getDefaultImplementation(NativeProxy::class.java)

                jennyProcessor.setProxyConfiguration(
                    JennyProxyConfiguration(
                        namespace = annotation.namespace,
                        allFields = annotation.allFields,
                        allMethods = annotation.allMethods,
                        onlyPublicMethod = false
                    )
                )
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
            .flatMap { annotation ->
                try {
                    annotation.classes
                    throw AssertionError("unreachable")
                } catch (e: MirroredTypesException) {
                    e.typeMirrors
                }.map {
                    jennyProcessor.setProxyConfiguration(
                        JennyProxyConfiguration(
                            namespace = annotation.namespace,
                            allFields = true,
                            allMethods = true,
                            onlyPublicMethod = true,
                        )
                    )
                    jennyProcessor.processForProxy(typeUtils.asElement(it) as TypeElement)
                }
            }
    }

    private fun generateJniHelper() {
        FileHandler.createOutputFile(
            outputDirectory,
            Constants.JENNY_GEN_DIR_PROXY + File.separatorChar + Constants.JENNY_JNI_HELPER_H_NAME
        ).use {
            it.write(Constants.JENNY_JNI_HELPER_H_CONTENT.toByteArray(Charsets.UTF_8))
        }
    }

    private fun generateFusionProxyHeader(name: String, proxyClasses: Collection<CppClass>) {
        FusionProxyGenerator(name, proxyClasses.sorted()).generate()
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return SUPPORTED_ANNOTATIONS
    }

    override fun getSupportedOptions(): Set<String> {
        return Configurations.ALL_OPTIONS
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