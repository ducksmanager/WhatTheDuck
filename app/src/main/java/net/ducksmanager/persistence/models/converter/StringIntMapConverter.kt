package net.ducksmanager.persistence.models.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class StringIntMapConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromString(value: String?): Map<String, Int>? {
            val mapType: Type = object : TypeToken<Map<String, Int>?>() {}.type
            return Gson().fromJson(value, mapType)
        }

        @TypeConverter
        @JvmStatic
        fun fromStringMap(map: Map<String, Int>?): String? {
            val gson = Gson()
            return gson.toJson(map)
        }
    }
}