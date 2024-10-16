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

package io.github.landerlyoung.jenny.utils

import io.github.landerlyoung.jenny.element.JennyElement
import io.github.landerlyoung.jenny.element.model.JennyModifier
import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.element.model.type.JennyType
import java.io.File
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Locale

fun JennyType.toJniReturnTypeString(): String {
    val locale = Locale.US
    return when {
        isPrimitive() -> "j${typeName.lowercase(locale)}"
        isArray() -> {
            if (componentType?.isPrimitive() == true) {
                "j${componentType!!.typeName.lowercase(locale)}Array"
            } else {
                "jobjectArray"
            }
        }

        jennyKind == JennyKind.VOID -> "void"
        jennyKind == JennyKind.DECLARED -> {
            when (typeName) {
                "java.lang.String" -> "jstring"
                "java.lang.Class" -> "jclass"
                "java.lang.Throwable" -> "jthrowable"
                else -> "jobject"
            }
        }

        else -> "jobject"
    }
}

fun JennyType.toJniTypeString(useJniHelper: Boolean, byPass: Boolean): String {
    val jniType = toJniReturnTypeString()
    return if (useJniHelper && this.needWrapLocalRef() && !byPass) {
        "const ::jenny::LocalRef<$jniType>&"
    } else {
        jniType
    }
}

fun JennyType.toJniCall(): String {
    val result = if (this.isPrimitive() || this.jennyKind == JennyKind.VOID) {
        this.typeName.lowercase()
    } else {
        "object"
    }
    return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun Collection<JennyModifier>.print(): String {
    return this.sorted()
        .joinToString(" ") { it.toString().lowercase(Locale.US) }
}

internal fun String.stripNonASCII(): String = this.replace("[^a-zA-Z0-9_]".toRegex()) {
    String.format(Locale.US, "_%05x", it.value.codePointAt(0))
}

fun JennyType.needWrapLocalRef(): Boolean {
    return (!isPrimitive() && jennyKind != JennyKind.VOID)
}

fun String.toCamelCase() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

internal fun String.toJniClassName(isNested: Boolean): String {

    val replaced = this.replace('.', '/')
    if (!isNested) return replaced

    val lastDotIndex = this.lastIndexOf('.')
    val dollarIndex = this.indexOf('$')

    return if (dollarIndex > lastDotIndex) {
        replaced
    } else {
        val outerClassEnd =
            this.lastIndexOf(".") // Last dot separates the outer class and inner class
        if (outerClassEnd != -1) {
            replaced.substring(0, outerClassEnd) + "$" + replaced.substring(outerClassEnd + 1)
        } else {
            replaced
        }
    }
}

fun JennyElement.isStatic() = JennyModifier.STATIC in modifiers
internal fun JennyElement.isConstant() = (isStatic() && JennyModifier.FINAL in modifiers)
internal fun JennyElement.isNative() = JennyModifier.NATIVE in modifiers
internal fun JennyElement.isPublic() = JennyModifier.PUBLIC in modifiers

internal fun <T> Collection<T>.visibility(onlyPublic: Boolean): Collection<T> where T : JennyElement {
    return if (onlyPublic) {
        this.filter { it.isPublic() }
    } else {
        this
    }
}

internal inline fun File.use(action: (OutputStream) -> Unit) {
    outputStream().use { outputStream ->
        action(outputStream)
    }
}

fun File.writeText(text: String, charset: Charset = Charsets.UTF_8) {
    this.use {
        it.write(text.toByteArray(charset))
    }
}

fun File.appendText(text: String, charset: Charset = Charsets.UTF_8) {
    this.outputStream().use { outputStream ->
        outputStream.write(text.toByteArray(charset))
    }
}

