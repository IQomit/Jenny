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

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.clazz.JennyClazzElement
import io.github.landerlyoung.jenny.element.method.JennyExecutableElement
import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.element.model.type.JennyType

class Signature(private val jennyElement: JennyElement) {

    private fun getSignatureClassName(type: JennyType): String {
        val output = StringBuilder()
        var jennyType = type

        while (jennyType.isArray()) {
            output.append('[')
            jennyType = (jennyType.componentType ?: error("Array type missing"))
        }

        when (jennyType.jennyKind) {
            JennyKind.BOOLEAN -> output.append('Z')
            JennyKind.BYTE -> output.append('B')
            JennyKind.CHAR -> output.append('C')
            JennyKind.SHORT -> output.append('S')
            JennyKind.INT -> output.append('I')
            JennyKind.LONG -> output.append('J')
            JennyKind.FLOAT -> output.append('F')
            JennyKind.DOUBLE -> output.append('D')
            JennyKind.VOID -> output.append('V')
            else -> output.append(
                'L' + type.getNonGenericType().typeName
                    .substringBefore("<")
                    .replace('.', '/')
            ).append(';')
        }
        return output.toString()
    }

    override fun toString(): String = buildString {
        if (jennyElement is JennyExecutableElement) {
            append('(')

            if (jennyElement.name.contentEquals("<init>")) {
                val clazz = jennyElement.declaringClass
                clazz?.declaringClass?.let {
                    if ((it as JennyClazzElement).isNestedClass) {
                        append(getSignatureClassName(clazz.type))
                    }
                }
            }

            for (param in jennyElement.parameters) {
                append(getSignatureClassName(param.type))
            }
            append(')')
            append(getSignatureClassName(jennyElement.returnType))
        } else {
            getSignatureClassName(jennyElement.type)
        }

    }

    companion object {
        fun getBinaryJennyElementSignature(element: JennyElement): String =
            Signature(element).toString()
    }
}