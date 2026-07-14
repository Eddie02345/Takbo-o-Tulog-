package com.github.eddie02345.takbootulog.domain

enum class VerdictType {
    TAKBO,
    ALANGANIN,
    TULOG_MUNA
}

data class Verdict(
    val type: VerdictType,
    val title: String,
    val colorHex: String,
    val description: String
)