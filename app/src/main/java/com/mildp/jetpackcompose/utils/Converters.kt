package com.mildp.jetpackcompose.utils

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromString(value: String): ArrayList<Int> {
        val list = ArrayList<Int>()
        val items = value.split(",")
        for (item in items) {
            list.add(item.toInt())
        }
        return list
    }

    @TypeConverter
    fun toString(list: ArrayList<Int>): String {
        return list.joinToString(",")
    }
}