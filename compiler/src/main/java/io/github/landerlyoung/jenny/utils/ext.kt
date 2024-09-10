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

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

internal fun KType.toJniTypeString(): String {
    val classifier = this.classifier as KClass<*>

    // Check if the type is a subclass of Throwable
    if (classifier.isSubclassOf(Throwable::class)) {
        return "jthrowable"
    }

    return when (classifier) {
        Boolean::class -> "jboolean"
        Byte::class -> "jbyte"
        Short::class -> "jshort"
        Int::class -> "jint"
        Long::class -> "jlong"
        Float::class -> "jfloat"
        Double::class -> "jdouble"
        Char::class -> "jchar"
        String::class -> "jstring"
        Class::class -> "jclass"
        Array::class -> {
            val componentType = this.arguments.firstOrNull()?.type?.classifier as? KClass<*>
            if (componentType?.isSubclassOf(Any::class) == true) {
                "jobjectArray"
            } else {
                when (componentType) {
                    Boolean::class -> "jbooleanArray"
                    Byte::class -> "jbyteArray"
                    Char::class -> "jcharArray"
                    Short::class -> "jshortArray"
                    Int::class -> "jintArray"
                    Long::class -> "jlongArray"
                    Float::class -> "jfloatArray"
                    Double::class -> "jdoubleArray"
                    else -> "jobjectArray"
                }
            }
        }

        else -> "jobject"
    }
}

fun String.stripNonASCII(): String = this.replace("[^a-zA-Z0-9_]".toRegex()) {
    String.format(Locale.US, "_%05x", it.value.codePointAt(0))
}