package com.fakeemergencyescape.call.data.local

object TemplateSeedData {
    fun all(): List<TemplateEntity> = listOf(
        TemplateEntity(
            id = "tpl-generic",
            category = "Generic",
            title = "Quick exit",
            message = "Hey, something came up. Can you step out for a minute?",
        ),
        TemplateEntity(
            id = "tpl-work",
            category = "Work",
            title = "Meeting reminder",
            message = "Your next meeting is starting now.",
        ),
        TemplateEntity(
            id = "tpl-family",
            category = "Family",
            title = "Check-in",
            message = "Can you come back home? I need your help.",
        ),
        TemplateEntity(
            id = "tpl-friend",
            category = "Friend",
            title = "Call me back",
            message = "Sorry, I need you to call me back as soon as possible.",
        ),
        TemplateEntity(
            id = "tpl-appointment",
            category = "Appointment",
            title = "Moved up",
            message = "Your appointment has been moved up.",
        ),
        TemplateEntity(
            id = "tpl-delivery",
            category = "Delivery",
            title = "Arrival",
            message = "I'm outside — can you come to the door?",
        ),
        TemplateEntity(
            id = "tpl-urgent",
            category = "Urgent (non-emergency)",
            title = "Need you now",
            message = "I need to talk to you right away — nothing serious, just urgent.",
        ),
    )
}
