package com.github.eddie02345.takbootulog.domain

object DecisionEngine {

    // WMO weather codes that mean "no rain currently falling," even if
    // precipitation_probability is high. Used to avoid false-positive rain
    // flags from probability alone.
    private val NO_RAIN_CODES = setOf(0, 1, 2, 3) // clear, mostly clear, partly cloudy, overcast
    private val FOG_CODES = setOf(45, 48)
    private val DRIZZLE_RAIN_CODES = (51..67) + (80..82) // drizzle, rain, rain showers
    private val STORM_CODES = 95..99

    fun evaluate(forecast: HourlyForecast): Verdict {
        val activeRedFlags = mutableListOf<String>()
        val activeWarningFlags = mutableListOf<String>()

        val isActuallyRaining = forecast.weatherCode in DRIZZLE_RAIN_CODES
                || forecast.weatherCode in STORM_CODES
                || forecast.rainVolume > 0.0

        // 1. AGGREGATE ALL RED FLAGS (TULOG MUNA CRITERIA)

        // Rain: require corroboration between probability and either
        // an actual rain-type weather_code or real rainVolume, so a high
        // probability over an "overcast, 0.00mm" hour doesn't false-positive.
        if ((forecast.rainProbability > 70 && isActuallyRaining) || forecast.rainVolume > 20.0) {
            activeRedFlags.add("umuulan nang malakas")
        }

        if (forecast.weatherCode in STORM_CODES) {
            activeRedFlags.add("may kulog't kidlat, delikado lumabas")
        }

        if (forecast.windSpeed > 40.0) {
            activeRedFlags.add("sobrang lakas ng hangin")
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

        // Light-to-moderate rain signal, but only counted as a warning if it's
        // not already claimed by the red-flag branch above.
        val isLightRainSignal = (forecast.rainProbability in 21..70 && isActuallyRaining)
                || (forecast.rainVolume > 0.0 && forecast.rainVolume <= 20.0)
        if (isLightRainSignal && "umuulan nang malakas" !in activeRedFlags) {
            activeWarningFlags.add("may kaunting ambon na baka lumakas")
        }

        // High rain probability but no corroborating rain code/volume — model is
        // hedging, not actually forecasting visible rain. Softer warning.
        if (forecast.rainProbability > 70 && !isActuallyRaining) {
            activeWarningFlags.add("mataas ang tsansa ng ulan pero maaliwalas pa naman ngayon")
        }

        if (forecast.weatherCode in FOG_CODES) {
            activeWarningFlags.add("maulap, medyo mahirap makita ang paligid")
        }

        if (forecast.windSpeed in 20.0..40.0) {
            activeWarningFlags.add("medyo mahangin, pero kaya pa")
        }

        // Warning band now covers PAGASA's Caution (27-32) through Extreme Caution
        // (33-39) heat index range, since this is where most PH daytime running
        // actually happens.
        if (forecast.feelsLikeTemperature in 27.0..39.0) {
            activeWarningFlags.add("maalinsangan ang hangin")
        } else if (forecast.feelsLikeTemperature < 18.0) {
            activeWarningFlags.add("medyo malamig, pero kaya pa")
        }
        if (forecast.uvIndex in 5.0..8.0) {
            activeWarningFlags.add("masakit sa balat ang sikat ng araw")
        }

        // 3. EVALUATE FINAL VERDICT STATUS AND STITCH TEXT DYNAMICALLY
        return when {
            activeRedFlags.isNotEmpty() -> {
                val combinedDescription = if (activeRedFlags.size > 1) {
                    "Combo disaster boss! Kasi ${activeRedFlags.distinct().joinToString(" at ")}. Tulog na lang muna, baka mapano ka pa."
                } else {
                    when (activeRedFlags.first()) {
                        "umuulan nang malakas" -> "Umuulan nang malakas boss! Lulusong ka ba o matutulog na lang?"
                        "mainit pa sa galit ng mama mo" -> "Mainit pa sa galit ng mama mo. Tulog na lang muna, baka ma-heatstroke ka."
                        "may kulog't kidlat, delikado lumabas" -> "May kulog't kidlat! Bawal lumabas, delikado."
                        "sobrang lakas ng hangin" -> "Sobrang lakas ng hangin ngayon. Delikado, tulog na lang muna."
                        else -> "Grabe ang sikat ng araw! Mapapaso balat mo boss. Tulog o takbong gabi na lang."
                    }
                }

                Verdict(
                    type = VerdictType.TULOG_MUNA,
                    title = "TULOG MUNA",
                    colorHex = "#E53935",
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
                        "mataas ang tsansa ng ulan pero maaliwalas pa naman ngayon" -> "Maaliwalas naman ngayon, pero medyo mataas ang tsansa ng ulan mamaya. Magdala ka ng payong just in case."
                        "maulap, medyo mahirap makita ang paligid" -> "Maulap ngayon, medyo mahirap makita ang paligid. Mag-ingat sa daan."
                        "medyo mahangin, pero kaya pa" -> "Medyo mahangin ngayon pero kaya pa naman. Takbo lang."
                        "masakit sa balat ang sikat ng araw" -> "Medyo masakit na sa balat ang sikat ng araw. Mag-sunblock ka muna bago lumabas!"
                        "medyo malamig, pero kaya pa" -> "Medyo malamig ngayon, pero kaya pa naman. Magdala ka lang ng jacket."
                        else -> "Medyo delikado ang kondisyon ngayon. Pwede namang tumakbo, pero hinay-hinay lang."
                    }
                }

                Verdict(
                    type = VerdictType.ALANGANIN,
                    title = "ALANGANIN",
                    colorHex = "#FBC02D",
                    description = combinedDescription
                )
            }

            else -> {
                Verdict(
                    type = VerdictType.TAKBO,
                    title = "TAKBO!",
                    colorHex = "#4CAF50",
                    description = "G na g boss! Swak na swak ang panahon para humataw. Takbo na!"
                )
            }
        }
    }
}