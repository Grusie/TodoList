package com.example.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.model.TodoDao
import com.example.todolist.model.TodoData
import com.example.todolist.model.TodoDatabase
import com.example.todolist.uiState.EditTodoAction
import com.example.todolist.uiState.TodoUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TodoListViewModel(application: Application) : AndroidViewModel(application) {
    private val _todoUiState = MutableStateFlow(TodoUiState())
    private val todoDb: TodoDatabase = TodoDatabase.getInstance(application)
    private val todoDao: TodoDao = todoDb.todoDao()

    val todoUiState: StateFlow<TodoUiState> = _todoUiState

    init {
        viewModelScope.launch {
            if (isActive) updateTodoList()
        }
    }


    fun dispatch(action: EditTodoAction) {
        when (action) {
            is EditTodoAction.ChangeTodoTitle -> {
                updateTodoTitle(action.title)
            }

            is EditTodoAction.ChangeTodoDescription -> {
                updateTodoDescription(action.description)
            }

            is EditTodoAction.UpdateTodoIsDone -> {
                updateTodoIsDone(action.id)
            }

            is EditTodoAction.ShowToastMsg -> {
                showToast()
            }

            is EditTodoAction.SetTodoData -> {
                setTodoData(action.id)
            }

            is EditTodoAction.ModifyTodoData -> {
                modifyTodoData(action.updateFlag)
            }

            is EditTodoAction.DeleteTodoItem -> {
                deleteTodoData(action.id)
            }
        }
    }

    /**
     * 투두데이터 세팅
     **/
    private fun setTodoData(id: Int) {
        if (id == -1) {
            _todoUiState.update {
                it.copy(
                    todoData = TodoData()
                )
            }
        } else {
            viewModelScope.launch {
                _todoUiState.update {
                    it.copy(
                        todoData = getTodoData(id)
                    )
                }
            }
        }
    }

    /**
     * 투두데이터 삽입/수정
     **/
    private fun modifyTodoData(updateFlag: Boolean = false) {
        viewModelScope.launch {
            if (updateFlag) updateTodoData() else insertTodoData()
        }
    }

    /**
     * 투두 데이터 삽입
     **/
    private suspend fun insertTodoData() {
        todoDao.insert(_todoUiState.value.todoData)
    }

    /**
     * 투두 데이터 타이틀 수정
     **/
    private fun updateTodoTitle(title: String) {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                todoData = currentTodoUiState.todoData.copy(title = title)
            )
        }
    }

    /**
     * 투두 데이터 내용 수정
     **/
    private fun updateTodoDescription(description: String) {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                todoData = currentTodoUiState.todoData.copy(description = description)
            )
        }
    }

    /**
     * 투두 데이터 체크 상태 업데이트
     **/
    private fun updateTodoIsDone(id: Int) {
        viewModelScope.launch {
            _todoUiState.update { currentTodoUiState ->
                val todoData = getTodoData(id)
                val updatedTodoData = todoData.copy(isDone = !todoData.isDone)
                if (updatedTodoData.isDone) currentTodoUiState.deleteDataSet.add(id) else currentTodoUiState.deleteDataSet.remove(
                    id
                )

                currentTodoUiState.copy(
                    todoData = updatedTodoData
                )
            }

            updateTodoData()
        }
    }

    /**
     * 토스트 메세지 관리
     **/
    private fun showToast() {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                isShownToast = true
            )
        }
        clearShowToast()
    }

    private fun clearShowToast() {
        viewModelScope.launch {
            delay(2000)
            _todoUiState.update { currentTodoUiState ->
                currentTodoUiState.copy(
                    isShownToast = false
                )
            }
        }
    }


    /**
     * 투두 리스트 업데이트
     **/
    private suspend fun updateTodoList() {
        todoDao.getAllTodoList().collect { todoList ->
            val updatedDeleteDatasSet = mutableSetOf<Int>()
            todoList.forEach {
                if (it.isDone) {
                    updatedDeleteDatasSet.add(it.id)
                }
            }
            _todoUiState.update { currentTodoUiState ->
                currentTodoUiState.copy(
                    todoList = todoList,
                    deleteDataSet = updatedDeleteDatasSet
                )
            }
        }
    }

    /**
     * 투두 데이터 업데이트
     **/
    private suspend fun updateTodoData() {
        todoDao.update(_todoUiState.value.todoData)
        _todoUiState.update { currentTodoUiState ->
            val updatedTodoList = currentTodoUiState.todoList.toMutableList()
            val index = updatedTodoList.indexOfFirst { it.id == _todoUiState.value.todoData.id }
            if (index != -1) {
                updatedTodoList[index] = _todoUiState.value.todoData
            }
            currentTodoUiState.copy(
                todoList = updatedTodoList
            )
        }
        //updateTodoList()
    }


    /**
     * 투두 데이터 삭제
     **/
    private fun deleteTodoData(id: Int = -1) {
        viewModelScope.launch {
            if (id != -1) {
                todoDao.delete(getTodoData(id))
            } else {
                _todoUiState.value.deleteDataSet.forEach { itemId ->
                    todoDao.delete(getTodoData(itemId))
                }
            }
        }
    }

    private suspend fun getTodoData(id: Int): TodoData {
        return if (id == -1) {
            TodoData()
        } else {
            todoDao.getTodoData(id).first()
        }
    }
}