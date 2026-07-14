package com.github.eddie02345.takbootulog.domain

object DecisionEngine {

    fun evaluate(forecast: HourlyForecast): Verdict {
        // 1. CHECK RED FLAGS (TULOG MUNA) FIRST

        if (forecast.rainProbability > 60 || forecast.rainVolume > 1.5) {
            return Verdict(
                type = VerdictType.TULOG_MUNA,
                title = "TULOG MUNA",
                colorHex = "#E53935", // Red
                description = "Umuulan nang malakas boss! Lulusong ka ba o matutulog na lang?"
            )
        }

        if (forecast.feelsLikeTemperature > 32.0) {
            return Verdict(
                type = VerdictType.TULOG_MUNA,
                title = "TULOG MUNA",
                colorHex = "#E53935",
                description = "Mainit pa sa galit ng mama mo. Tulog na lang muna, baka ma-heatstroke ka."
            )
        }

        if (forecast.uvIndex > 8.0) {
            return Verdict(
                type = VerdictType.TULOG_MUNA,
                title = "TULOG MUNA",
                colorHex = "#E53935",
                description = "Grabe ang sikat ng araw! Mapapaso balat mo boss. Tulog o takbong gabi na lang."
            )
        }

        // 2. CHECK WARNING FLAGS (ALANGANIN) NEXT
        if (forecast.relativeHumidity > 85 && forecast.feelsLikeTemperature > 26.0) {
            return Verdict(
                type = VerdictType.ALANGANIN,
                title = "ALANGANIN",
                colorHex = "#FBC02D", // Yellow
                description = "Hindi naman umuulan pero parang tatakbo ka sa loob ng sinigang. Sobrang lagkit!"
            )
        }

        if (forecast.rainProbability in 21..60 || (forecast.rainVolume > 0.0 && forecast.rainVolume <= 1.5)) {
            return Verdict(
                type = VerdictType.ALANGANIN,
                title = "ALANGANIN",
                colorHex = "#FBC02D",
                description = "May kaunting ambon. Pwede pa naman, pero baka maging aquatic animal ka pag biglang bumuhos."
            )
        }


        if (forecast.feelsLikeTemperature in 25.0..32.0 || forecast.feelsLikeTemperature < 12.0) {
            return Verdict(
                type = VerdictType.ALANGANIN,
                title = "ALANGANIN",
                colorHex = "#FBC02D",
                description = "Medyo maalinsangan ang hangin ngayon. Pwede namang tumakbo, pero hinay-hinay lang."
            )
        }

        if (forecast.uvIndex in 5.0..8.0) {
            return Verdict(
                type = VerdictType.ALANGANIN,
                title = "ALANGANIN",
                colorHex = "#FBC02D",
                description = "Medyo masakit na sa balat ang sikat ng araw. Mag-sunblock ka muna bago lumabas!"
            )
        }

        // 3. PERFECT CONDITIONS (TAKBO)
        return Verdict(
            type = VerdictType.TAKBO,
            title = "TAKBO!",
            colorHex = "#4CAF50", // Green
            description = "G na g boss! Swak na swak ang panahon para humataw. Takbo na!"
        )
    }
}