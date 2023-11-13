package com.example.todolist.uiState

import com.example.todolist.model.TodoData
import java.time.LocalDate

/**
 * 투두 데이터 UI STATE
 **/
data class TodoUiState(
    val todoList: List<TodoData> = listOf(),
    val todoData: TodoData = TodoData(),
    val isShownToast: Boolean = false,
    val deleteDataSet: MutableSet<Int> = mutableSetOf(),
    val currentDate: LocalDate = LocalDate.now()
)