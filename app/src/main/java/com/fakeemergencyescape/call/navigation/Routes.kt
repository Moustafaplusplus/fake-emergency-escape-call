package com.fakeemergencyescape.call.navigation

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val EDIT = "edit/{fakeCallId}"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"

    const val INCOMING = "incoming/{fakeCallId}"
    const val ACTIVE = "active/{fakeCallId}"

    const val ARG_FAKE_CALL_ID = "fakeCallId"

    fun incoming(fakeCallId: String) = "incoming/$fakeCallId"
    fun active(fakeCallId: String) = "active/$fakeCallId"
    fun edit(fakeCallId: String) = "edit/$fakeCallId"
}
