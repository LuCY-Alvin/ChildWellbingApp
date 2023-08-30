package com.mildp.jetpackcompose.model

import com.mildp.jetpackcompose.utils.Helper
import java.util.*

data class ForegroundStats(
    val app: String?,
    val category: String?,
    private val startTime: Calendar,
    private val endTime: Calendar
) {

    val endTimeString: String
        get() = Helper().timeString(endTime.timeInMillis)
    val startTimeString: String
        get() = Helper().timeString(startTime.timeInMillis)

    override fun toString(): String {
        return """
               App: $app
               Category: $category
               start: ${Helper().timeString(startTime.timeInMillis)}
               end: ${Helper().timeString(endTime.timeInMillis)}
               """.trimIndent()
    }

    companion object {
        fun fromEventType(start: EventTypeData, end: EventTypeData): ForegroundStats {
            return ForegroundStats(
                start.app,
                start.category,
                start.time,
                end.time
            )
        }
    }
}
