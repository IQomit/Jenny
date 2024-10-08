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

import io.github.landerlyoung.jenny.utils.FileHandler

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2020-10-21
 * Time:   15:48
 * Life with Passion, Code with Creativity.
 * ```
 */

class FusionProxyGenerator(
    private val name: String,
    private val proxyClasses: Collection<CppClass>
) {
    fun generate() {
        FileHandler.createOutputFile(Constants.JENNY_GEN_DIR_PROXY, name).use {
            buildString {
                generateSourceContent()
            }.let { content ->
                it.write(content.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun StringBuilder.generateSourceContent() {
        append(Constants.AUTO_GENERATE_NOTICE)
        append(
            """
                |#pragma once
                |
                |#include <jni.h>
                |
                |""".trimMargin()
        )

        proxyClasses.forEach {
            append(
                """
                |#include "${it.headerFileName}"
                |
            """.trimMargin()
            )
        }

        append(
            """
            |
            |namespace jenny {
            |
            |inline bool initAllProxies(JNIEnv* env) {
            |
            |   bool success = 
            |
        """.trimMargin()
        )

        append(proxyClasses.joinToString(" &&\n") {
            "        " + it.namespace + "::" + it.name + "::initClazz(env)"
        })
        append(";")

        append(
            """
            |
            |   return success;
            |}
            |
            |} // end of namespace jenny
            |
        """.trimMargin()
        )
    }
}