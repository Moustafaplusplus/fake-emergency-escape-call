package com.fakeemergencyescape.call.domain.model

enum class CallStatus {
    DRAFT,
    SCHEDULED,
    RINGING,
    ANSWERED,
    DECLINED,
    MISSED,
    CANCELLED,
    COMPLETED,
    ;

    companion object {
        fun fromString(value: String): CallStatus =
            entries.find { it.name == value } ?: DRAFT
    }
}
