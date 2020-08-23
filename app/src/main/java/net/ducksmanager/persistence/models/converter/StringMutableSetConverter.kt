package net.ducksmanager.persistence.models.converter

import androidx.room.TypeConverter

class StringMutableSetConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMutableSet(value: MutableSet<String>): String = value.joinToString(",")

        @TypeConverter
        @JvmStatic
        fun toMutableSet(value: String): MutableSet<String> = value.split(",").toMutableSet()
    }
}