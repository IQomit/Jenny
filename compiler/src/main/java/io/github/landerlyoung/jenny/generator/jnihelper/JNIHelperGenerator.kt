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
package io.github.landerlyoung.jenny.generator.jnihelper

import io.github.landerlyoung.jenny.Constants
import io.github.landerlyoung.jenny.generator.Generator
import io.github.landerlyoung.jenny.generator.OutputTargetConfigurator
import io.github.landerlyoung.jenny.utils.FileHandler
import java.io.File
import java.io.IOException

internal class JNIHelperGenerator(
    private var outputDirectory: String,
    private val jniHelperName: String,
) : Generator<Unit, Unit>,
    OutputTargetConfigurator {

    override fun generate(input: Unit) {
        writeFileContent(Constants.JENNY_JNI_HELPER_H_CONTENT, jniHelperName)
    }

    private fun writeFileContent(content: String, fileName: String) {
        try {
            FileHandler.createOutputFile(
                outputDirectory,
                JENNY_GEN_DIR_PROXY + File.separatorChar + fileName
            ).use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }
        } catch (e: IOException) {
            println("Error writing file $fileName: ${e.message}")
        }
    }

    override fun setOutputTargetPath(outputPath: String) {
        outputDirectory = outputPath
    }

    companion object {
        private const val JENNY_GEN_DIR_PROXY = "jenny.proxy"
    }
}