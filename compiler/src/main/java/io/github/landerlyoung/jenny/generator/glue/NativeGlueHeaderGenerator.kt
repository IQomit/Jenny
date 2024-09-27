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
import io.github.landerlyoung.jenny.utils.JennyHeaderDefinitionsProvider

internal class NativeGlueHeaderGenerator(private val registerJniMethods: Boolean = true) :
    Generator<HeaderData, String> {
    //TODO: Use template instead of NativeHeaderDefinitions predefined Strings

    override fun generate(input: HeaderData) = createHeader(input)

    private fun createHeader(input: HeaderData): String {
        val classInfo = input.classInfo
        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)
            append(JennyHeaderDefinitionsProvider.getHeaderInitForGlue(classInfo))
            if (registerJniMethods) {
                append(JennyHeaderDefinitionsProvider.getConstantsDefinitions(input.constants))
                append(JennyHeaderDefinitionsProvider.getNativeMethodsDefinitions(classInfo, input.methods))
                append(JennyHeaderDefinitionsProvider.getJniRegister(input.methods))
                append(JennyHeaderDefinitionsProvider.getEndNameSpace(classInfo.simpleClassName))
            } else {
                append(JennyHeaderDefinitionsProvider.getConstantsDefinitions(input.constants))
                append(JennyHeaderDefinitionsProvider.getEndNameSpace(classInfo.simpleClassName))
                append(JennyHeaderDefinitionsProvider.getNativeMethodsDefinitions(classInfo, input.methods))
            }
        }
    }
}