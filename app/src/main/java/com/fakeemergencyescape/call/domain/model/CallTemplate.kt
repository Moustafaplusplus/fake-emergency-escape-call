package com.fakeemergencyescape.call.domain.model

data class CallTemplate(
    val id: String,
    val category: String,
    val title: String,
    val message: String,
    val scriptJson: String,
    val suggestedCallerName: String,
) {
    val script: CallScript? get() = CallScriptCodec.decode(scriptJson)
    val scriptLineCount: Int get() = script?.lineCount ?: 1
}
