package net.ducksmanager.persistence.models.converter

import androidx.room.TypeConverter

class StringListConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromInstant(value: List<String>): String = value.joinToString(",")

        @TypeConverter
        @JvmStatic
        fun toInstant(value: String): List<String> = value.split(",").toList()
    }
}