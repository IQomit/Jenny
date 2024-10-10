package io.github.landerlyoung.jenny.utils

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2020-10-21
 * Time:   15:43
 * Life with Passion, Code with Creativity.
 * ```
 */

data class CppClass(
        val name: String,
        val namespace: String,
        val headerFileName: String
) : Comparable<CppClass> {

    override fun compareTo(other: CppClass) = compareValuesBy(
            this, other,
            { it.namespace },
            { it.name })
}