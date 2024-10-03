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
import java.util.*

internal fun JennyType.toJniReturnTypeString(): String {
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

internal fun JennyType.toJniCall(): String {
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

internal fun JennyType.needWrapLocalRef(): Boolean {
    return (!isPrimitive() && jennyKind != JennyKind.VOID)
}

internal fun String.toCamelCase() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

internal fun JennyElement.isStatic() = JennyModifier.STATIC in modifiers
internal fun JennyElement.isConstant() = isStatic() && JennyModifier.FINAL in modifiers
internal fun JennyElement.isNative() = JennyModifier.NATIVE in modifiers
internal fun JennyElement.isPublic() = JennyModifier.PUBLIC in modifiers

internal fun <T> Collection<T>.visibility(onlyPublic: Boolean): Collection<T> where T : JennyElement {
    return if (onlyPublic) {
        this.filter { it.isPublic() }
    } else {
        this
    }
}

