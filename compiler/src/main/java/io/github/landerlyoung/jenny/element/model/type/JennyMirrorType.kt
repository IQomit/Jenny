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

package io.github.landerlyoung.jenny.element.model.type

import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

internal class JennyMirrorType(private val mirrorType: TypeMirror) : JennyType {
    override val typeName: String
        get() = mirrorType.toString()

    override val jennyKind: JennyKind
        get() = JennyKind.fromMirrorType(mirrorType)

    override fun isPrimitive() = mirrorType.kind.isPrimitive

    override fun isArray() = mirrorType.kind == TypeKind.ARRAY

    override val componentType: JennyType?
        get() = if (mirrorType is ArrayType) {
            JennyMirrorType(mirrorType.componentType)
        } else {
            null
        }

}