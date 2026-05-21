package com.fakeemergencyescape.call.ui.util

object CallDisplayUtils {
    fun initials(name: String): String =
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
}
