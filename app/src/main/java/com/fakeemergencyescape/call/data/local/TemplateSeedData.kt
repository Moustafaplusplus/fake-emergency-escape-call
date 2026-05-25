package com.fakeemergencyescape.call.data.local

import com.fakeemergencyescape.call.domain.model.CallScript
import com.fakeemergencyescape.call.domain.model.CallScriptCodec
import com.fakeemergencyescape.call.domain.model.CallScriptLine

object TemplateSeedData {
    fun all(): List<TemplateEntity> = listOf(
        scenario(
            id = "tpl-home-flood",
            category = "Home emergency",
            title = "Water heater burst",
            suggestedCallerName = "Jess",
            preview = "Hey, you there? … water everywhere … come home now.",
            lines = listOf(
                line("Hey, you there?", 3_500),
                line("Where are you right now?", 4_500),
                line("Listen, the water heater just burst. There's water everywhere in the hallway.", 3_000),
                line("The building manager is on his way, but I need you home now. Can you leave wherever you are and come back?", 0),
            ),
        ),
        scenario(
            id = "tpl-kid-sick",
            category = "Family",
            title = "School pickup — sick child",
            suggestedCallerName = "Mom",
            preview = "The school called — Sam is sick. I can't get there. Please leave and pick them up now.",
            lines = listOf(
                line("Hi honey, sorry to call during your day.", 3_000),
                line("Where are you?", 4_000),
                line("The school just called. Sam threw up twice and has a fever. They need someone to pick them up in the next twenty minutes.", 3_500),
                line("I can't get there from work. Please leave wherever you are and go get them now.", 0),
            ),
        ),
        scenario(
            id = "tpl-smoke-alarm",
            category = "Home emergency",
            title = "Smoke alarm won't stop",
            suggestedCallerName = "Alex",
            preview = "Smoke alarm going off for ten minutes … smell something burnt … come home now.",
            lines = listOf(
                line("Hey! Can you hear me?", 3_000),
                line("Are you still out? Where are you right now?", 4_500),
                line("The smoke alarm has been going off for ten minutes. I smell something burnt near the kitchen.", 3_500),
                line("I'm freaking out. Please come home right now. I can't find the source.", 0),
            ),
        ),
        scenario(
            id = "tpl-pet-emergency",
            category = "Family",
            title = "Dog escaped",
            suggestedCallerName = "Chris",
            preview = "Max got out toward the main road. Come home now and help me find him.",
            lines = listOf(
                line("Hey, it's me.", 3_000),
                line("Where are you right now?", 4_000),
                line("Max got out. The gate was left open and he ran toward the main road.", 3_500),
                line("I need you to come home now and help me find him before it gets dark.", 0),
            ),
        ),
        scenario(
            id = "tpl-car-towed",
            category = "Urgent",
            title = "Car being towed",
            suggestedCallerName = "Jordan",
            preview = "There's a tow truck hooking up your car on our street. You need to get here in five minutes.",
            lines = listOf(
                line("Hey, quick question. Where's your car parked?", 3_500),
                line("I just looked out the window and there's a tow truck hooking up your car on our street.", 4_000),
                line("They're loading it right now. You need to leave and get here in the next five minutes or it's gone.", 0),
            ),
        ),
        scenario(
            id = "tpl-elderly-parent",
            category = "Family",
            title = "Parent fell — ambulance",
            suggestedCallerName = "Sarah",
            preview = "Dad fell. Ambulance is coming. Someone needs to meet them and grab his insurance papers.",
            lines = listOf(
                line("Hey, sorry to bother you.", 3_000),
                line("Are you at the office? Where are you?", 4_000),
                line("I just got off the phone with Dad. He fell in the bathroom and the neighbor called an ambulance.", 3_500),
                line("I'm heading to the hospital, but someone needs to meet them there and grab his insurance papers from home. Can you leave and handle it?", 0),
            ),
        ),
        scenario(
            id = "tpl-landlord-leak",
            category = "Home emergency",
            title = "Leak into apartment below",
            suggestedCallerName = "Taylor",
            preview = "Landlord is here with a plumber. Leak from our unit. They need someone inside now.",
            lines = listOf(
                line("Hey, you need to hear this.", 3_000),
                line("Where are you right now?", 4_000),
                line("The landlord just showed up with a plumber. There's a leak coming from our unit into the apartment below.", 3_500),
                line("They need someone inside the apartment now or they'll shut off water to the whole building. Please come home.", 0),
            ),
        ),
        scenario(
            id = "tpl-power-freezer",
            category = "Home emergency",
            title = "Power out — fridge failing",
            suggestedCallerName = "Dad",
            preview = "Power's been out two hours. Freezer is thawing. Need you home to reset the breaker.",
            lines = listOf(
                line("Hey, it's Dad.", 3_000),
                line("Where are you? Still at that thing tonight?", 4_500),
                line("The power's been out for two hours and the freezer is starting to thaw. I tried the breaker but it won't stay on.", 3_500),
                line("I need you to come home now and take a look before we lose everything in the fridge.", 0),
            ),
        ),
    )

    private fun line(text: String, pauseAfterMs: Long): CallScriptLine =
        CallScriptLine(text = text, pauseAfterMs = pauseAfterMs)

    private fun scenario(
        id: String,
        category: String,
        title: String,
        suggestedCallerName: String,
        preview: String,
        lines: List<CallScriptLine>,
    ): TemplateEntity {
        val script = CallScript(lines)
        return TemplateEntity(
            id = id,
            category = category,
            title = title,
            message = preview,
            scriptJson = CallScriptCodec.encode(script),
            suggestedCallerName = suggestedCallerName,
        )
    }
}
