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

package io.github.landerlyoung.jenny.utils

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStream

internal class FileHandler(private val file: File) {
    constructor(filePath: String) : this(File(filePath))
    constructor(packageName: String, name: String) : this(
        File(packageName.replace('.', File.separatorChar), name)
    )

    fun createFile(): Boolean {
        return try {
            if (!file.exists()) {
                file.apply { parentFile?.mkdirs() }.createNewFile()
            } else {
                false // File already exists
            }
        } catch (e: IOException) {
            println("Error creating file: ${e.message}")
            false
        }
    }

    fun writeToFile(data: String, append: Boolean = false) {
        try {
            if (ensureFileExists()) {
                BufferedWriter(FileWriter(file, append)).use { writer ->
                    writer.write(data)
                    writer.newLine()
                }
            }
        } catch (e: IOException) {
            println("Error writing to file: ${e.message}")
        }
    }

    fun appendToFile(data: String) = writeToFile(data, append = true)

    fun readFromFile(): String {
        return if (file.exists()) {
            file.readText()
        } else {
            println("File does not exist.")
            ""
        }
    }

    fun readLines(): List<String> {
        return if (file.exists()) {
            file.readLines()
        } else {
            println("File does not exist.")
            emptyList()
        }
    }

    fun deleteFile(): Boolean {
        return if (file.exists()) {
            file.delete()
        } else {
            println("File does not exist.")
            false
        }
    }

    fun exists(): Boolean = file.exists()

    fun size(): Long {
        return if (file.exists()) {
            file.length()
        } else {
            println("File does not exist.")
            0L
        }
    }

    fun lastModified(): Long {
        return if (file.exists()) {
            file.lastModified()
        } else {
            println("File does not exist.")
            0L
        }
    }

    private fun ensureFileExists(): Boolean {
        return if (!file.exists()) {
            println("File does not exist.")
            false
        } else {
            true
        }
    }

    companion object {
        fun createOutputStreamFrom(path: String): OutputStream {
            return try {
                File(path).apply { parentFile.mkdirs() }.outputStream().buffered()
            } catch (e: IOException) {
                println("Error creating output stream: ${e.message}")
                throw e
            }
        }

        fun createOutputStreamFrom(parent: String, name: String): OutputStream {
            return try {
                File(parent, name).apply { parentFile.mkdirs() }.outputStream().buffered()
            } catch (e: IOException) {
                println("Error creating output stream: ${e.message}")
                throw e
            }
        }

        fun createOutputFile(parent: String, name: String, overwrite: Boolean = false): File {
            return try {
                val file = File(parent, name)
                file.parentFile.mkdirs()
                if (file.exists() && overwrite) {
                    file.delete()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                file
            } catch (e: IOException) {
                println("Error creating output stream: ${e.message}")
                throw e
            }
        }
    }
}
