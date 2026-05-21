package com.fakeemergencyescape.call.navigation

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val EDIT = "edit/{fakeCallId}"
    const val DUPLICATE = "duplicate/{sourceCallId}"

    const val ARG_SOURCE_CALL_ID = "sourceCallId"
    const val SETTINGS = "settings"
    const val CALL_APPEARANCE = "call_appearance"
    const val ACTIVE_CALL_APPEARANCE = "active_call_appearance"
    const val ONBOARDING = "onboarding"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"

    const val INCOMING = "incoming/{fakeCallId}"

    const val ARG_FAKE_CALL_ID = "fakeCallId"

    fun incoming(fakeCallId: String) = "incoming/$fakeCallId"

    fun previewIncoming() = incoming(com.fakeemergencyescape.call.ui.preview.PreviewCallData.INCOMING_ID)

    fun previewActive() = incoming(com.fakeemergencyescape.call.ui.preview.PreviewCallData.ACTIVE_ID)

    fun edit(fakeCallId: String) = "edit/$fakeCallId"
    fun duplicate(sourceCallId: String) = "duplicate/$sourceCallId"
}
