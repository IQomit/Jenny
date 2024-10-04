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

package io.github.landerlyoung.jenny.processor

import io.github.landerlyoung.jenny.element.clazz.JennyClassElement
import io.github.landerlyoung.jenny.element.clazz.JennyClassTypeElement
import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.generator.proxy.NativeProxyGenerator
import io.github.landerlyoung.jenny.generator.proxy.ProxyConfiguration
import io.github.landerlyoung.jenny.provider.proxy.factory.ProxyProviderType
import io.github.landerlyoung.jenny.utils.CppFileHelper
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass


class NativeProxyProcessor(outputDirectory: String) : Processor {

    private val proxyConfiguration = ProxyConfiguration(
        threadSafe = true,
        useJniHelper = true,
        headerOnlyProxy = false,
        allFields = true,
        onlyPublicMethod = true
    )
    private val templatesPath = System.getProperty("user.dir") + "/compiler/src/main/resources/jte"
    private val cppFileHelper = CppFileHelper()
    private val nativeProxyGenerator =
        NativeProxyGenerator(
            type = ProxyProviderType.Template(templatesPath),
            cppFileHelper = cppFileHelper,
            outputDirectory = outputDirectory
        ).apply {
            setConfiguration(proxyConfiguration)
        }

    override fun process(namespace: String, input: Any) {
        cppFileHelper.setNamespace(namespace)
        nativeProxyGenerator.generate(makeJennyClazz(input))
    }

    private fun makeJennyClazz(input: Any): JennyClazzElement {
        return when (input) {
            is KClass<*> -> JennyClassElement(input.java)
            is Class<*> -> JennyClassElement(input)
            is TypeElement -> JennyClassTypeElement(input)
            else -> throw IllegalArgumentException("${input.javaClass.name} input type is not supported")
        }
    }


}