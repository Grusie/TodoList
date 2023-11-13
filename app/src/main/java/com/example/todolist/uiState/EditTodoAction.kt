package com.example.todolist.uiState

import java.time.LocalDate

sealed class EditTodoAction {

    /**
     * UI 관련
     **/
    data class ChangeTodoTitle(val title: String) : EditTodoAction()
    data class ChangeTodoDescription(val description: String) : EditTodoAction()
    data class ModifyTodoData(val updateFlag: Boolean) : EditTodoAction()
    object ShowToastMsg : EditTodoAction()
    data class UpdateTodoIsDone(val id: Int) : EditTodoAction()
    data class UpdateCurrentDate(val date: LocalDate) : EditTodoAction()

    /**
     * DB 관련
     **/
    data class SetTodoData(val id: Int) : EditTodoAction()
    data class DeleteTodoItem(val id: Int) : EditTodoAction()

    data class UpdateTodoList(val selectedDate: LocalDate) : EditTodoAction()
}