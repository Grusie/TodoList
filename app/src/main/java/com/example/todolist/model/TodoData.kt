package com.example.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var todoTime: String = "",
    var writtenTime: String = "",
    var isNotification: Boolean = false,
    var isDone: Boolean = false
)
