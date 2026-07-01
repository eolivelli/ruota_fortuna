package com.ruota.uicommon.format

/** Formats a dollar amount as "$1,200" with thousands separators. Negative-safe. */
fun formatMoney(amount: Int): String {
    val sign = if (amount < 0) "-" else ""
    val digits = kotlin.math.abs(amount).toString()
    val grouped = buildString {
        val n = digits.length
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) append(',')
            append(digits[i])
        }
    }
    return "$sign\$$grouped"
}
