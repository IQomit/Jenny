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

package io.github.landerlyoung.jenny.generator

import io.github.landerlyoung.jenny.Constants
import io.github.landerlyoung.jenny.HandyHelper
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

internal data class HeaderData(
    val classInfo: ClassInfo,
    val methods: Sequence<KFunction<*>>,
    val constants: Sequence<KProperty1<out Any, *>>
)

internal class NativeHeaderGenerator : Generator<HeaderData, String> {

    override fun generate(input: HeaderData): String {
        val headerName = createHeaderName(input.classInfo.simpleClassName)
        println("Writing header file $headerName")
        return createHeader(input)
    }

    private fun createHeader(input: HeaderData): String {
        val classInfo = input.classInfo
        return buildString {
            append(Constants.AUTO_GENERATE_NOTICE)
            append(
                """
                |
                |/* C++ header file for class ${classInfo.slashClassName} */
                |#pragma once
                |
                |#include <jni.h>
                |
                |namespace ${classInfo.simpleClassName} {
                |
                |// DO NOT modify
                |static constexpr auto FULL_CLASS_NAME = u8"$${classInfo.slashClassName}";
                |
                |""".trimMargin()
            )
            append(getConstantsDefinitions(input.constants))
        }
    }

    // TODO: Finish this up
    private fun getConstantsDefinitions(constants: Sequence<KProperty1<out Any, *>>): String {
        val outputString = StringBuilder()
        constants.forEach {
            ////
        }
    }

    private fun createHeaderName(simpleClassName: String): String {
        TODO("Not yet implemented")
    }
}