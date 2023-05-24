package com.example.newsapp.data.db

import androidx.room.TypeConverter
import com.example.newsapp.data.api.model.Source

class Converter {

    @TypeConverter
    fun fromSource(source: Source): String? {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}