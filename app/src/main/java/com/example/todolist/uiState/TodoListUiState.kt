package com.example.todolist.uiState

import com.example.todolist.model.TodoData
import java.time.LocalDate

data class TodoListUiState(
    val todoList: List<TodoData> = listOf(),
    val currentDate: LocalDate = LocalDate.now(),
    val deleteDataSet: MutableSet<Int> = mutableSetOf()
)
