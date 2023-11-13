package com.example.todolist

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN)
val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

class DateConverter {
    @TypeConverter
    fun stringToDate(date: String?): LocalDate? {
        if (date == null) return null

        return date.stringToDate()
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        if (date == null) return null

        return date.dateToString()
    }

    @TypeConverter
    fun stringToTime(time: String?): LocalTime? {
        if (time == null) return null

        return time.stringToTime()
    }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? {
        if (time == null) return null

        return time.timeToString()
    }

    @TypeConverter
    fun longToDateTime(dateTime: String?): LocalDateTime? {
        if (dateTime == null) return null

        return dateTime.stringToDateTime()
    }

    @TypeConverter
    fun dateTimeToLong(dateTime: LocalDateTime?): String? {
        if (dateTime == null) return null

        return dateTime.dateTimeToString()
    }
}

fun LocalDate.dateToString(formatter: DateTimeFormatter = dateFormat): String {
    return this.format(formatter)
}

fun String.stringToDate(formatter: DateTimeFormatter = dateFormat): LocalDate {
    return LocalDate.parse(this, formatter)
}

fun LocalTime.timeToString(formatter: DateTimeFormatter = timeFormat): String {
    return this.format(formatter)
}

fun String.stringToTime(formatter: DateTimeFormatter = timeFormat): LocalTime {
    return LocalTime.parse(this, formatter)
}

fun LocalDateTime.dateTimeToString(formatter: DateTimeFormatter = dateTimeFormatter): String {
    return this.format(formatter)
}

fun String.stringToDateTime(formatter: DateTimeFormatter = dateTimeFormatter): LocalDateTime {
    return LocalDateTime.parse(this, formatter)
}