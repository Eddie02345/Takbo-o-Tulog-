package com.github.eddie02345.takbootulog.domain

object DecisionEngine {

    fun evaluate(forecast: HourlyForecast): Verdict {
        val activeRedFlags = mutableListOf<String>()
        val activeWarningFlags = mutableListOf<String>()

        // 1. AGGREGATE ALL RED FLAGS (TULOG MUNA CRITERIA)
        // Rain: PAGASA calls 15-30mm/hr "heavy", 30+ "intense/torrential".
        // >20mm/hr is a reasonable don't go out line.
        if (forecast.rainProbability > 70 || forecast.rainVolume > 20.0) {
            activeRedFlags.add("umuulan nang malakas")
        }
        // Heat: PAGASA heat index tiers - Caution 27-32, Extreme Caution 33-41,
        // Danger 42-51.
        if (forecast.feelsLikeTemperature > 39.0) {
            activeRedFlags.add("mainit pa sa galit ng mama mo")
        }
        if (forecast.uvIndex > 8.0) {
            activeRedFlags.add("mapapaso ang balat mo sa tindi ng UV index")
        }

        // 2. AGGREGATE ALL WARNING FLAGS (ALANGANIN CRITERIA)
        if (forecast.relativeHumidity > 85 && forecast.feelsLikeTemperature > 26.0) {
            activeWarningFlags.add("parang tatakbo ka sa loob ng sinigang sa lagkit")
        }
        // Light-to-moderate rain (PAGASA: <2.5mm/hr light, 2.5-7.5 moderate) is a
        // warning, not a stop-sign.
        if (forecast.rainProbability in 21..70 || (forecast.rainVolume > 0.0 && forecast.rainVolume <= 20.0)) {
            activeWarningFlags.add("may kaunting ambon na baka lumakas")
        }
        // Warning band now covers PAGASA's Caution (27-32) through Extreme Caution
        // (33-39) heat index range, since this is where most PH daytime running
        // actually happens.
        if (forecast.feelsLikeTemperature in 27.0..39.0) {
            activeWarningFlags.add("maalinsangan ang hangin")
        } else if (forecast.feelsLikeTemperature < 18.0) {
            // <12.0 almost never fires outside Baguio/Sagada/Tagaytay; loosened so
            // it can actually trigger for lowland mornings too.
            activeWarningFlags.add("medyo malamig, pero kaya pa")
        }
        if (forecast.uvIndex in 5.0..8.0) {
            activeWarningFlags.add("masakit sa balat ang sikat ng araw")
        }

        // 3. EVALUATE FINAL VERDICT STATUS AND STITCH TEXT DYNAMICALLY
        return when {
            activeRedFlags.isNotEmpty() -> {
                // If there are multiple disasters, join them using "at"
                val combinedDescription = if (activeRedFlags.size > 1) {
                    "Combo disaster boss! Kasi ${activeRedFlags.joinToString(" at ")}. Tulog na lang muna, baka mapano ka pa."
                } else {
                    // Fallback to original standalone phrases
                    when (activeRedFlags.first()) {
                        "umuulan nang malakas" -> "Umuulan nang malakas boss! Lulusong ka ba o matutulog na lang?"
                        "mainit pa sa galit ng mama mo" -> "Mainit pa sa galit ng mama mo. Tulog na lang muna, baka ma-heatstroke ka."
                        else -> "Grabe ang sikat ng araw! Mapapaso balat mo boss. Tulog o takbong gabi na lang."
                    }
                }

                Verdict(
                    type = VerdictType.TULOG_MUNA,
                    title = "TULOG MUNA",
                    colorHex = "#E53935", // Full Red
                    description = combinedDescription
                )
            }

            activeWarningFlags.isNotEmpty() -> {
                val combinedDescription = if (activeWarningFlags.size > 1) {
                    "Medyo alanganin boss: ${activeWarningFlags.distinct().joinToString(", ")}. Pwede naman sumabak pero ingat lang."
                } else {
                    when (activeWarningFlags.first()) {
                        "parang tatakbo ka sa loob ng sinigang sa lagkit" -> "Hindi naman umuulan pero parang tatakbo ka sa loob ng sinigang. Sobrang lagkit!"
                        "may kaunting ambon na baka lumakas" -> "May kaunting ambon. Pwede pa naman, pero baka maging aquatic animal ka pag biglang bumuhos."
                        "masakit sa balat ang sikat ng araw" -> "Medyo masakit na sa balat ang sikat ng araw. Mag-sunblock ka muna bago lumabas!"
                        "medyo malamig, pero kaya pa" -> "Medyo malamig ngayon, pero kaya pa naman. Magdala ka lang ng jacket."
                        else -> "Medyo delikado ang kondisyon ngayon. Pwede namang tumakbo, pero hinay-hinay lang."
                    }
                }

                Verdict(
                    type = VerdictType.ALANGANIN,
                    title = "ALANGANIN",
                    colorHex = "#FBC02D", // Warning Yellow
                    description = combinedDescription
                )
            }

            else -> {
                Verdict(
                    type = VerdictType.TAKBO,
                    title = "TAKBO!",
                    colorHex = "#4CAF50", // Perfect Green
                    description = "G na g boss! Swak na swak ang panahon para humataw. Takbo na!"
                )
            }
        }
    }
}