package net.ducksmanager.persistence.models.converter

import androidx.room.TypeConverter

class StringMutableSetConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromInstant(value: MutableSet<String>): String = value.joinToString(",")

        @TypeConverter
        @JvmStatic
        fun toInstant(value: String): MutableSet<String> = value.split(",").toMutableSet()
    }
}