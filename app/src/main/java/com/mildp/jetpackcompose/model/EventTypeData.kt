package com.mildp.jetpackcompose.model

import java.util.*

data class EventTypeData(
    val type: Int,
    val app: String?,
    val category: String?,
    val time: Calendar
)
