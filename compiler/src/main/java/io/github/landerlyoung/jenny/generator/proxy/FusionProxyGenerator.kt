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
package io.github.landerlyoung.jenny.generator.proxy

import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.OutputTargetConfigurator
import io.github.landerlyoung.jenny.utils.Constants
import io.github.landerlyoung.jenny.utils.CppClass
import io.github.landerlyoung.jenny.utils.FileHandler
import java.io.File

internal class FusionProxyGenerator(
    private var outputDirectory: String,
    private var name: String
) : Generator<CppClass, Unit>, OutputTargetConfigurator {
    val file = FileHandler.createOutputFile(
        outputDirectory,
        Constants.JENNY_GEN_DIR_PROXY + File.separatorChar + name,
        true
    )
    override fun generate(input: CppClass) {
        if (name.isEmpty())
            return
        val existingContent = if (file.exists()) file.readText(Charsets.UTF_8) else ""
        file.writeText(generateSourceContent(input, existingContent))
    }

    private fun generateSourceContent(proxyClass: CppClass, existingContent: String): String {
        val stringBuilder = StringBuilder()
        if (existingContent.isEmpty()) {
            stringBuilder.append(Constants.AUTO_GENERATE_NOTICE)
            stringBuilder.append(jniHeader)
            stringBuilder.append(
                """
                |#include "${proxyClass.headerFileName}"
                |
            """.trimMargin()
            )
        } else {
            val modified = existingContent.replace(existingContent.substringAfterLast(".h\""),"\n")
            stringBuilder.append(modified)
            stringBuilder.append(
                """
                |#include "${proxyClass.headerFileName}"
                |
            """.trimMargin()
            )
        }
        stringBuilder.append(start)

        // If the existing content already contains an init block, append to it
        if (existingContent.contains(start) && existingContent.contains(end)) {
            // Insert the new initClazz between the start and end markers
            val existingInitBlock = existingContent.substringAfter(start).substringBefore(end).trim()
            stringBuilder.append(existingInitBlock)
            if (existingInitBlock.isNotEmpty()) {
                stringBuilder.append("\n        && ")
            }
            stringBuilder.append("${proxyClass.namespace}::${proxyClass.name}::initClazz(env)")
        } else {
            stringBuilder.append("        ${proxyClass.namespace}::${proxyClass.name}::initClazz(env)")
        }
        stringBuilder.append(end)

        return stringBuilder.toString()
    }

    override fun setOutputTargetPath(outputPath: String) {
        outputDirectory = outputPath
    }

    fun setHeaderName(headerName: String) {
        name = headerName
    }
    companion object {
        private val jniHeader =  """
                |#pragma once
                |
                |#include <jni.h>
                |
                """.trimMargin()
        private val start =      """
            |
            |namespace jenny {
            |
            |inline bool initAllProxies(JNIEnv* env) {
            |
            |   bool success =
            |
        """.trimMargin()
        private val end =          """;
            |
            |   return success;
            |}
            |
            |} // end of namespace jenny
            |
        """.trimMargin()
    }
}