package com.appforlife.filemanager.utils

import java.util.*

fun String?.default(default: String): String {
    return this ?: default
}

fun String?.defaultEmpty(): String {
    return this.default("")
}

fun Int?.default(default: Int): Int {
    return this ?: default
}

fun Int?.defaultZero(): Int {
    return this.default(0)
}

fun Long?.default(default: Long): Long {
    return this ?: default
}

fun Long?.defaultZero(): Long {
    return this.default(0L)
}

fun Double?.default(default: Double): Double {
    return this ?: default
}

fun Double?.defaultZero(): Double {
    return this.default(0.0)
}

fun Float?.default(default: Float): Float {
    return this ?: default
}

fun Float?.defaultZero(): Float {
    return this.default(0f)
}

fun <T> List<T>?.defaultEmpty(): List<T> {
    return this ?: listOf()
}


fun Date?.default(default: Date): Date {
    return this ?: default
}

fun Date?.defaultToday(): Date {
    return this.default(Date())
}

fun Boolean?.default(default: Boolean): Boolean {
    return this ?: default
}

fun Boolean?.defaultFalse(): Boolean {
    return this.default(false)
}

fun Boolean?.defaultTrue(): Boolean {
    return this.default(true)
}

fun <T> T?.default(default: T): T {
    return this ?: default
}

inline fun <T, R> T.mapOb(transform: (T) -> R?): R? {
    return transform(this)
}