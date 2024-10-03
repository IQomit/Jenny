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

package io.github.landerlyoung.jenny.generator.glue

import io.github.landerlyoung.jenny.Constants
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.HeaderData
import io.github.landerlyoung.jenny.provider.JennyHeaderDefinitionsProvider

internal class NativeGlueHeaderGenerator(
    private val registerJniMethods: Boolean = true,
    private val jennyHeaderDefinitionsProvider: JennyHeaderDefinitionsProvider
) : Generator<HeaderData, String> {

    override fun generate(input: HeaderData) = createHeader(input)

    private fun createHeader(input: HeaderData): String {
        val classInfo = input.classInfo
        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)
            append(jennyHeaderDefinitionsProvider.getHeaderInitForGlue(classInfo, input.namespace.startOfNamespace))
            if (registerJniMethods) {
                append(jennyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
                append(jennyHeaderDefinitionsProvider.getNativeMethodsDefinitions(classInfo, input.methods))
                append(jennyHeaderDefinitionsProvider.getJniRegister(input.methods))
                append(
                    jennyHeaderDefinitionsProvider.getEndNameSpace(
                        className = classInfo.simpleClassName,
                        endNamespace = input.namespace.endOfNameSpace
                    )
                )
            } else {
                append(jennyHeaderDefinitionsProvider.getConstantsIdDeclare(input.constants))
                append(
                    jennyHeaderDefinitionsProvider.getEndNameSpace(
                        className = classInfo.simpleClassName,
                        endNamespace = input.namespace.endOfNameSpace
                    )
                )
                append(jennyHeaderDefinitionsProvider.getNativeMethodsDefinitions(classInfo, input.methods))
            }
        }
    }
}