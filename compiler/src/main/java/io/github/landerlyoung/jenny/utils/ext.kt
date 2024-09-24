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

import io.github.landerlyoung.jenny.element.model.type.JennyKind
import io.github.landerlyoung.jenny.element.model.type.JennyType
import java.util.*
import javax.lang.model.type.PrimitiveType

internal fun JennyType.toJniTypeString(): String {
    if (isPrimitive())
        return "j${typeName.lowercase(Locale.US)}"
    if (isArray()) {
        return if (componentType is PrimitiveType) {
            "j${componentType!!.typeName.lowercase(Locale.US)}Array"
        } else {
            "jobjectArray"
        }
    }
    if (jennyKind == JennyKind.VOID)
        return "void"
    if (jennyKind == JennyKind.DECLARED) {
        //todo : finish this on
    }
    return "jobject"
}

fun String.stripNonASCII(): String = this.replace("[^a-zA-Z0-9_]".toRegex()) {
    String.format(Locale.US, "_%05x", it.value.codePointAt(0))
}

